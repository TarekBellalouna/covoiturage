package com.covoiturage.trajet.dto;

import com.covoiturage.trajet.Trajet;

import java.time.LocalDateTime;

public record TrajetResponse(
        Long id,
        String statut,
        Long conducteurId,
        String conducteurPrenom,
        String vehiculeMarque,
        String vehiculeModele,
        int placesVehicule,
        String pointDepartNom,
        LocalDateTime dateCreation,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        int nombrePassagers
) {
    public static TrajetResponse from(Trajet t, int nombrePassagers) {
        return new TrajetResponse(
                t.getId(),
                t.getStatut().name(),
                t.getConducteur().getId(),
                t.getConducteur().getPrenom(),
                t.getVehicule().getMarque(),
                t.getVehicule().getModele(),
                t.getVehicule().getNombrePlaces(),
                t.getPointDepart().getNom(),
                t.getDateCreation(),
                t.getDateDebut(),
                t.getDateFin(),
                nombrePassagers);
    }
}
