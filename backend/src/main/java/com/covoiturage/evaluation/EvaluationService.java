package com.covoiturage.evaluation;

import com.covoiturage.demande.DemandeCovoiturage;
import com.covoiturage.evaluation.dto.EvaluationRequest;
import com.covoiturage.evaluation.dto.ReputationResponse;
import com.covoiturage.trajet.StatutTrajet;
import com.covoiturage.trajet.Trajet;
import com.covoiturage.trajet.TrajetRepository;
import com.covoiturage.user.Utilisateur;
import com.covoiturage.user.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluations;
    private final TrajetRepository trajets;
    private final UtilisateurRepository utilisateurs;

    @Transactional
    public void noter(Long auteurId, Long trajetId, EvaluationRequest req) {
        Trajet trajet = trajets.findById(trajetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trajet introuvable"));
        if (trajet.getStatut() != StatutTrajet.TERMINE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "On ne peut noter qu'un trajet termine");
        }

        boolean auteurEstConducteur = trajet.getConducteur().getId().equals(auteurId);
        Long cibleId;
        TypeEvaluation type;

        if (auteurEstConducteur) {
            // Le conducteur note un passager precis du trajet.
            if (req.cibleId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indiquez le passager a noter (cibleId)");
            }
            if (!estPassagerDuTrajet(trajet, req.cibleId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce passager n'appartient pas au trajet");
            }
            cibleId = req.cibleId();
            type = TypeEvaluation.CONDUCTEUR_VERS_PASSAGER;
        } else {
            // Un passager note le conducteur.
            if (!estPassagerDuTrajet(trajet, auteurId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas participe a ce trajet");
            }
            cibleId = trajet.getConducteur().getId();
            type = TypeEvaluation.PASSAGER_VERS_CONDUCTEUR;
        }

        if (evaluations.existsByTrajetIdAndAuteurIdAndCibleId(trajetId, auteurId, cibleId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez deja note cette personne pour ce trajet");
        }

        Utilisateur auteur = utilisateurs.getReferenceById(auteurId);
        Utilisateur cible = utilisateurs.findById(cibleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cible introuvable"));

        Evaluation e = new Evaluation();
        e.setNote(req.note());
        e.setCommentaire(req.commentaire());
        e.setType(type);
        e.setAuteur(auteur);
        e.setCible(cible);
        e.setTrajet(trajet);
        evaluations.save(e);
    }

    public ReputationResponse reputation(Long utilisateurId) {
        Double moyenne = evaluations.moyenneByCibleId(utilisateurId);
        long nombre = evaluations.countByCibleId(utilisateurId);
        return new ReputationResponse(utilisateurId, moyenne != null ? moyenne : 0.0, nombre);
    }

    private boolean estPassagerDuTrajet(Trajet trajet, Long passagerId) {
        for (DemandeCovoiturage d : trajet.getDemandes()) {
            if (d.getPassager().getId().equals(passagerId)) {
                return true;
            }
        }
        return false;
    }
}
