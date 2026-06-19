package com.covoiturage.carte.dto;

/** Element affiche sur la carte : une demande en attente ou un trajet actif. */
public record ActiviteCarteResponse(
        String type,        // DEMANDE ou bien TRAJET
        String statut,
        String label,
        double latitude,
        double longitude
) {}
