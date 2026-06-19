package com.covoiturage.trajet;

import com.covoiturage.demande.DemandeCovoiturage;
import com.covoiturage.demande.StatutDemande;
import com.covoiturage.evaluation.Evaluation;
import com.covoiturage.evaluation.EvaluationRepository;
import com.covoiturage.notification.NotificationService;
import com.covoiturage.trajet.dto.TrajetDetailResponse;
import com.covoiturage.trajet.dto.TrajetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrajetService {

    private final TrajetRepository trajets;
    private final EvaluationRepository evaluations;
    private final NotificationService notifications;

    @Transactional(readOnly = true)
    public TrajetResponse detail(Long id) {
        Trajet t = charger(id);
        return TrajetResponse.from(t, compterPassagers(t));
    }

    @Transactional(readOnly = true)
    public TrajetDetailResponse details(Long requesterId, Long trajetId) {
        Trajet t = charger(trajetId);
        boolean autorise = t.getConducteur().getId().equals(requesterId)
                || t.getDemandes().stream().anyMatch(d -> d.getPassager().getId().equals(requesterId));
        if (!autorise) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces non autorise a ce trajet");
        }

        var conducteur = new TrajetDetailResponse.ConducteurInfo(
                t.getConducteur().getId(), t.getConducteur().getPrenom(),
                t.getConducteur().getNom(), t.getConducteur().getTelephone());

        var v = t.getVehicule();
        var vehicule = new TrajetDetailResponse.VehiculeInfo(
                v.getMarque(), v.getModele(), v.getCouleur(), v.getImmatriculation(), v.getNombrePlaces());

        var p = t.getPointDepart();
        var pointDepart = new TrajetDetailResponse.PointInfo(p.getNom(), p.getLatitude(), p.getLongitude());

        List<TrajetDetailResponse.PassagerInfo> passagers = t.getDemandes().stream()
                .map(d -> new TrajetDetailResponse.PassagerInfo(
                        d.getPassager().getId(), d.getPassager().getPrenom(), d.getPassager().getNom(),
                        d.getDestinationTexte(), d.getDestinationLat(), d.getDestinationLng(),
                        d.getNombrePlacesDemandees(), d.getStatut().name()))
                .toList();

        List<TrajetDetailResponse.EvaluationInfo> evals = evaluations.findByTrajetId(trajetId).stream()
                .map((Evaluation e) -> new TrajetDetailResponse.EvaluationInfo(
                        e.getAuteur().getPrenom(), e.getCible().getPrenom(),
                        e.getNote(), e.getCommentaire(), e.getType().name(), e.getDate()))
                .toList();

        return new TrajetDetailResponse(
                t.getId(), t.getStatut().name(), t.getDateCreation(), t.getDateDebut(), t.getDateFin(),
                conducteur, vehicule, pointDepart, passagers, evals);
    }

    @Transactional(readOnly = true)
    public List<TrajetResponse> mesTrajets(Long conducteurId) {
        return trajets.findByConducteurIdOrderByDateCreationDesc(conducteurId).stream()
                .map(t -> TrajetResponse.from(t, compterPassagers(t)))
                .toList();
    }

    @Transactional
    public TrajetResponse demarrer(Long conducteurId, Long trajetId) {
        Trajet t = chargerSiConducteur(conducteurId, trajetId);
        exigerStatut(t, StatutTrajet.OUVERT);
        t.setStatut(StatutTrajet.EN_COURS);
        t.setDateDebut(LocalDateTime.now());
        propagerAuxDemandes(t, StatutDemande.EN_COURS);
        return TrajetResponse.from(t, compterPassagers(t));
    }

    @Transactional
    public TrajetResponse terminer(Long conducteurId, Long trajetId) {
        Trajet t = chargerSiConducteur(conducteurId, trajetId);
        exigerStatut(t, StatutTrajet.EN_COURS);
        t.setStatut(StatutTrajet.TERMINE);
        t.setDateFin(LocalDateTime.now());
        propagerAuxDemandes(t, StatutDemande.TERMINEE);
        return TrajetResponse.from(t, compterPassagers(t));
    }

    @Transactional
    public TrajetResponse annuler(Long conducteurId, Long trajetId) {
        Trajet t = chargerSiConducteur(conducteurId, trajetId);
        if (t.getStatut() == StatutTrajet.TERMINE || t.getStatut() == StatutTrajet.ANNULE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce trajet ne peut plus etre annule");
        }
        t.setStatut(StatutTrajet.ANNULE);
        propagerAuxDemandes(t, StatutDemande.ANNULEE);
        return TrajetResponse.from(t, compterPassagers(t));
    }

    private void propagerAuxDemandes(Trajet t, StatutDemande nouveau) {
        for (DemandeCovoiturage d : t.getDemandes()) {
            if (d.getStatut() == StatutDemande.ACCEPTEE || d.getStatut() == StatutDemande.EN_COURS) {
                d.setStatut(nouveau);
                notifications.notifierPassager(d);
            }
        }
    }

    private int compterPassagers(Trajet t) {
        return (int) t.getDemandes().stream()
                .filter(d -> d.getStatut() != StatutDemande.ANNULEE)
                .count();
    }

    private void exigerStatut(Trajet t, StatutTrajet attendu) {
        if (t.getStatut() != attendu) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Operation impossible dans l'etat " + t.getStatut());
        }
    }

    private Trajet chargerSiConducteur(Long conducteurId, Long trajetId) {
        Trajet t = charger(trajetId);
        if (!t.getConducteur().getId().equals(conducteurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce trajet n'est pas le votre");
        }
        return t;
    }

    private Trajet charger(Long id) {
        return trajets.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trajet introuvable"));
    }
}
