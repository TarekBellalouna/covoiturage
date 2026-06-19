package com.covoiturage.demande.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AccepterRequest(
        @NotNull Long vehiculeId,
        /** Optionnel : rattacher la demande a un trajet OUVERT existant (covoiturage partage). */
        Long trajetId,
        @NotNull @Positive Double prix
) {}