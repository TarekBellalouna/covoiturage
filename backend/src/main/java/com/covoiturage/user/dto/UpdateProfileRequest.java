package com.covoiturage.user.dto;

/** Modification du profil. Les champs nuls sont ignores. */
public record UpdateProfileRequest(
        String nom,
        String prenom,
        String telephone,
        String photoUrl
) {}
