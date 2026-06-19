package com.covoiturage.user.dto;

/** Vue publique d'un utilisateur renvoyee par l'API (jamais le mot de passe). */
public record UserResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone,
        String photoUrl,
        boolean estConducteur,
        String modeActif,
        String numeroPermis
) {}
