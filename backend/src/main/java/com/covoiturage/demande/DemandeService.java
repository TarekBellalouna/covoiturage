package com.covoiturage.demande;

import com.covoiturage.demande.dto.AccepterRequest;
import com.covoiturage.demande.dto.DemandeRequest;
import com.covoiturage.demande.dto.DemandeResponse;
import com.covoiturage.notification.NotificationService;
import com.covoiturage.pointrencontre.PointDeRencontre;
import com.covoiturage.pointrencontre.PointDeRencontreRepository;
import com.covoiturage.trajet.StatutTrajet;
import com.covoiturage.trajet.Trajet;
import com.covoiturage.trajet.TrajetRepository;
import com.covoiturage.user.Utilisateur;
import com.covoiturage.user.UtilisateurRepository;
import com.covoiturage.vehicule.Vehicule;
import com.covoiturage.vehicule.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.covoiturage.position.PositionConducteur;
import com.covoiturage.position.PositionConducteurRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemandeService {

    /** Duree de validite d'une demande avant expiration automatique. */
    private static final long EXPIRATION_MINUTES = 5;

    private static final double RAYON_METRES = 4000;

    private final DemandeRepository demandes;
    private final UtilisateurRepository utilisateurs;
    private final PointDeRencontreRepository points;
    private final VehiculeRepository vehicules;
    private final TrajetRepository trajets;
    private final NotificationService notifications;
    private final PositionConducteurRepository positions;

    @Transactional
    public DemandeResponse creer(Long passagerId, DemandeRequest req) {
        Utilisateur passager = utilisateurs.findById(passagerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        PointDeRencontre point = points.findById(req.pointRencontreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Point de rencontre introuvable"));

        DemandeCovoiturage d = new DemandeCovoiturage();
        d.setPassager(passager);
        d.setPointRencontre(point);
        d.setDestinationTexte(req.destinationTexte());
        d.setDestinationLat(req.destinationLat());
        d.setDestinationLng(req.destinationLng());
        d.setNombrePlacesDemandees(req.nombrePlacesDemandees());
        d.setStatut(StatutDemande.EN_ATTENTE);
        d.setDateExpiration(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        demandes.save(d);

        // Push temps reel vers les conducteurs proches.
        notifications.notifierConducteursProches(d);

        return DemandeResponse.from(d);
    }

    public DemandeResponse detail(Long id) {
        return DemandeResponse.from(charger(id));
    }

    public List<DemandeResponse> mesDemandes(Long passagerId) {
        return demandes.findByPassagerIdOrderByDateCreationDesc(passagerId).stream()
                .map(DemandeResponse::from)
                .toList();
    }

    /**
     * Demandes en attente PROCHES de la position du conducteur (vide s'il est hors
     * ligne).
     */
    @Transactional(readOnly = true)
    public List<DemandeResponse> enAttente(Long conducteurId) {
        return positions.findByConducteurId(conducteurId)
                .filter(PositionConducteur::isDisponible)
                .map(pos -> demandes
                        .trouverDemandesProches(conducteurId, pos.getLatitude(), pos.getLongitude(), RAYON_METRES)
                        .stream()
                        .map(DemandeResponse::from)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional
    public void annuler(Long passagerId, Long demandeId) {
        DemandeCovoiturage d = charger(demandeId);
        if (!d.getPassager().getId().equals(passagerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette demande n'est pas la votre");
        }
        if (d.getStatut() != StatutDemande.EN_ATTENTE && d.getStatut() != StatutDemande.ACCEPTEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette demande ne peut plus etre annulee");
        }
        d.setStatut(StatutDemande.ANNULEE);
    }

    /**
     * Acceptation par un conducteur. Cree un trajet OUVERT (ou rattache a un trajet
     * existant
     * pour le covoiturage partage), puis valide la transition EN_ATTENTE ->
     * ACCEPTEE de maniere
     * atomique grace au verrou optimiste (@Version).
     */
    @Transactional
    public DemandeResponse accepter(Long conducteurId, Long demandeId, AccepterRequest req) {
        DemandeCovoiturage d = charger(demandeId);
        if (d.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette demande n'est plus disponible");
        }

        Utilisateur conducteur = utilisateurs.findById(conducteurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conducteur introuvable"));
        Vehicule vehicule = vehicules.findById(req.vehiculeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicule introuvable"));
        if (!vehicule.getProprietaire().getId().equals(conducteurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce vehicule ne vous appartient pas");
        }

        Trajet trajet = resoudreTrajet(conducteurId, vehicule, d, req.trajetId());

        // Verification des places disponibles.
        long dejaPrises = trajet.getId() != null
                ? demandes.placesOccupees(trajet.getId(), StatutDemande.ANNULEE)
                : 0;
        if (dejaPrises + d.getNombrePlacesDemandees() > vehicule.getNombrePlaces()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plus assez de places dans le vehicule");
        }

        // Transition atomique : si deux conducteurs acceptent en meme temps,
        // le second declenche une OptimisticLockingFailureException au flush.
        d.setStatut(StatutDemande.ACCEPTEE);
        d.setTrajet(trajet);
        d.setPrix(req.prix());

        try {
            demandes.saveAndFlush(d);
        } catch (OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette demande vient d'etre prise par un autre conducteur");
        }

        notifications.notifierPassager(d);
        return DemandeResponse.from(d);
    }

    private Trajet resoudreTrajet(Long conducteurId, Vehicule vehicule, DemandeCovoiturage d, Long trajetId) {
        if (trajetId != null) {
            Trajet existant = trajets.findById(trajetId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trajet introuvable"));
            if (!existant.getConducteur().getId().equals(conducteurId)
                    || existant.getStatut() != StatutTrajet.OUVERT) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce trajet n'accepte plus de passagers");
            }
            return existant;
        }
        Trajet nouveau = new Trajet();
        nouveau.setConducteur(vehicule.getProprietaire());
        nouveau.setVehicule(vehicule);
        nouveau.setPointDepart(d.getPointRencontre());
        nouveau.setStatut(StatutTrajet.OUVERT);
        return trajets.save(nouveau);
    }

    /** Tache planifiee : expire les demandes restees trop longtemps en attente. */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void expirerDemandes() {
        demandes.expirerDemandesDepassees(LocalDateTime.now());
    }

    private DemandeCovoiturage charger(Long id) {
        return demandes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable"));
    }
}
