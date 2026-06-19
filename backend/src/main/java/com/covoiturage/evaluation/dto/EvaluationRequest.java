package com.covoiturage.evaluation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record EvaluationRequest(
        @Min(1) @Max(5) int note,
        String commentaire,
        /** Requis seulement quand un conducteur note un passager precis du trajet. */
        Long cibleId
) {}
