package com.covoiturage.vehicule;

import com.covoiturage.user.Utilisateur;
import com.covoiturage.user.UtilisateurRepository;
import com.covoiturage.vehicule.dto.VehiculeRequest;
import com.covoiturage.vehicule.dto.VehiculeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculeService {

    private final VehiculeRepository vehicules;
    private final UtilisateurRepository utilisateurs;

    @Transactional
    public VehiculeResponse ajouter(Long userId, VehiculeRequest req) {
        Utilisateur u = utilisateurs.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Vehicule v = new Vehicule();
        v.setMarque(req.marque());
        v.setModele(req.modele());
        v.setCouleur(req.couleur());
        v.setImmatriculation(req.immatriculation());
        v.setNombrePlaces(req.nombrePlaces());
        v.setProprietaire(u);
        vehicules.save(v);

        // Le premier vehicule debloque la capacite conducteur.
        if (!u.isEstConducteur()) {
            u.setEstConducteur(true);
            utilisateurs.save(u);
        }
        return VehiculeResponse.from(v);
    }

    public List<VehiculeResponse> mesVehicules(Long userId) {
        return vehicules.findByProprietaireId(userId).stream()
                .map(VehiculeResponse::from)
                .toList();
    }

    @Transactional
    public VehiculeResponse modifier(Long userId, Long vehiculeId, VehiculeRequest req) {
        Vehicule v = chargerSiProprietaire(userId, vehiculeId);
        v.setMarque(req.marque());
        v.setModele(req.modele());
        v.setCouleur(req.couleur());
        v.setImmatriculation(req.immatriculation());
        v.setNombrePlaces(req.nombrePlaces());
        return VehiculeResponse.from(v);
    }

    @Transactional
    public void supprimer(Long userId, Long vehiculeId) {
        Vehicule v = chargerSiProprietaire(userId, vehiculeId);
        vehicules.delete(v);
    }

    private Vehicule chargerSiProprietaire(Long userId, Long vehiculeId) {
        Vehicule v = vehicules.findById(vehiculeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicule introuvable"));
        if (!v.getProprietaire().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce vehicule ne vous appartient pas");
        }
        return v;
    }
}
