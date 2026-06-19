package com.covoiturage.demande.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DemandeRequest(
                @NotNull Long pointRencontreId,
                @NotBlank String destinationTexte,
                Double destinationLat,
                Double destinationLng,
                @Min(1) @Max(4) int nombrePlacesDemandees) {
}
