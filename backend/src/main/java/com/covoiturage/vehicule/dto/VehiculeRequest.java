package com.covoiturage.vehicule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record VehiculeRequest(
        @NotBlank String marque,
        @NotBlank String modele,
        String couleur,
        String immatriculation,
        @Min(1) int nombrePlaces
) {}
