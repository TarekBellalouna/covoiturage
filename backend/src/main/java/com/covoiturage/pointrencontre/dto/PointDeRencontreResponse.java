package com.covoiturage.pointrencontre.dto;

import com.covoiturage.pointrencontre.PointDeRencontre;

public record PointDeRencontreResponse(
        Long id,
        String nom,
        double latitude,
        double longitude,
        String ville
) {
    public static PointDeRencontreResponse from(PointDeRencontre p) {
        return new PointDeRencontreResponse(p.getId(), p.getNom(), p.getLatitude(), p.getLongitude(), p.getVille());
    }
}
