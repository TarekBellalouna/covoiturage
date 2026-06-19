package com.covoiturage.evaluation.dto;

public record ReputationResponse(
        Long utilisateurId,
        double noteMoyenne,
        long nombreAvis
) {}
