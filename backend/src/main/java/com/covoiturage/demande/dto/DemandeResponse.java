package com.covoiturage.demande.dto;

import com.covoiturage.demande.DemandeCovoiturage;

import java.time.LocalDateTime;

public record DemandeResponse(
        Long id,
        Long passagerId,
        String passagerPrenom,
        String passagerPhotoUrl,
        String passagerTelephone,
        Long pointRencontreId,
        String pointRencontreNom,
        double latitude,
        double longitude,
        String destinationTexte,
        Double destinationLat,
        Double destinationLng,
        Double prix,
        int nombrePlacesDemandees,
        String statut,
        LocalDateTime dateCreation,
        Long trajetId) {
    public static DemandeResponse from(DemandeCovoiturage d) {
        return new DemandeResponse(
                d.getId(),
                d.getPassager().getId(),
                d.getPassager().getPrenom(),
                d.getPassager().getPhotoUrl(),
                d.getPassager().getTelephone(),
                d.getPointRencontre().getId(),
                d.getPointRencontre().getNom(),
                d.getPointRencontre().getLatitude(),
                d.getPointRencontre().getLongitude(),
                d.getDestinationTexte(),
                d.getDestinationLat(),
                d.getDestinationLng(),
                d.getPrix(),
                d.getNombrePlacesDemandees(),
                d.getStatut().name(),
                d.getDateCreation(),
                d.getTrajet() != null ? d.getTrajet().getId() : null);
    }
}