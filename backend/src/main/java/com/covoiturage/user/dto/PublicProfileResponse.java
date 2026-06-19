package com.covoiturage.user.dto;

/** Profil consultable par un autre utilisateur. */
public record PublicProfileResponse(
        Long id,
        String prenom,
        String nom,
        String photoUrl,
        String telephone,
        boolean estConducteur,
        double noteMoyenne,
        long nombreAvis,
        long nombreTrajetsConducteur,
        long nombreTrajetsPassager
) {}
