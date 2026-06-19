package com.covoiturage.vehicule.dto;

import com.covoiturage.vehicule.Vehicule;

public record VehiculeResponse(
        Long id,
        String marque,
        String modele,
        String couleur,
        String immatriculation,
        int nombrePlaces,
        boolean actif
) {
    public static VehiculeResponse from(Vehicule v) {
        return new VehiculeResponse(v.getId(), v.getMarque(), v.getModele(),
                v.getCouleur(), v.getImmatriculation(), v.getNombrePlaces(), v.isActif());
    }
}
