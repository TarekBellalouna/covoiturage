package com.covoiturage.carte;

import com.covoiturage.carte.dto.ActiviteCarteResponse;
import com.covoiturage.demande.DemandeCovoiturage;
import com.covoiturage.demande.DemandeRepository;
import com.covoiturage.demande.StatutDemande;
import com.covoiturage.trajet.StatutTrajet;
import com.covoiturage.trajet.Trajet;
import com.covoiturage.trajet.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/carte")
@RequiredArgsConstructor
public class CarteController {

    private final DemandeRepository demandes;
    private final TrajetRepository trajets;

    /** Demandes en attente + trajets actifs, pour affichage colore sur la carte. */
    @GetMapping("/activite")
    @Transactional(readOnly = true)
    public List<ActiviteCarteResponse> activite() {
        List<ActiviteCarteResponse> resultat = new ArrayList<>();

        for (DemandeCovoiturage d : demandes.findByStatut(StatutDemande.EN_ATTENTE)) {
            var p = d.getPointRencontre();
            resultat.add(new ActiviteCarteResponse(
                    "DEMANDE", "EN_ATTENTE",
                    p.getNom() + " → " + d.getDestinationTexte(),
                    p.getLatitude(), p.getLongitude()));
        }

        for (Trajet t : trajets.findByStatutIn(List.of(StatutTrajet.OUVERT, StatutTrajet.EN_COURS))) {
            var p = t.getPointDepart();
            resultat.add(new ActiviteCarteResponse(
                    "TRAJET", t.getStatut().name(),
                    "Trajet · " + p.getNom(),
                    p.getLatitude(), p.getLongitude()));
        }

        return resultat;
    }
}
