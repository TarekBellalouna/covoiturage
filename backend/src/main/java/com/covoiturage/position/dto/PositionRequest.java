package com.covoiturage.position.dto;

import jakarta.validation.constraints.NotNull;

public record PositionRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull Boolean disponible
) {}
