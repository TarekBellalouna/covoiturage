package com.covoiturage.auth.dto;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String modeActif
) {}
