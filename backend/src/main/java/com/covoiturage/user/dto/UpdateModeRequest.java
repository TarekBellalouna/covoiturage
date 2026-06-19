package com.covoiturage.user.dto;

import com.covoiturage.user.ModeUtilisateur;
import jakarta.validation.constraints.NotNull;

public record UpdateModeRequest(
        @NotNull ModeUtilisateur mode
) {}
