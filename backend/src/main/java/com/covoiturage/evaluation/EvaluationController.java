package com.covoiturage.evaluation;

import com.covoiturage.evaluation.dto.EvaluationRequest;
import com.covoiturage.evaluation.dto.ReputationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /** Note l'autre partie a la fin d'un trajet (le type est deduit du role de l'auteur). */
    @PostMapping("/api/trajets/{trajetId}/evaluations")
    public void noter(@AuthenticationPrincipal String userId,
                      @PathVariable Long trajetId,
                      @Valid @RequestBody EvaluationRequest request) {
        evaluationService.noter(Long.valueOf(userId), trajetId, request);
    }

    /** Reputation d'un utilisateur : note moyenne et nombre d'avis recus. */
    @GetMapping("/api/users/{id}/reputation")
    public ReputationResponse reputation(@PathVariable Long id) {
        return evaluationService.reputation(id);
    }
}
