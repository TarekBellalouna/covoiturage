package com.covoiturage.trajet.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vue detaillee d'un trajet : conducteur, vehicule, passagers et evaluations.
 */
public record TrajetDetailResponse(
        Long id,
        String statut,
        LocalDateTime dateCreation,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        ConducteurInfo conducteur,
        VehiculeInfo vehicule,
        PointInfo pointDepart,
        List<PassagerInfo> passagers,
        List<EvaluationInfo> evaluations) {
    public record ConducteurInfo(Long id, String prenom, String nom, String telephone) {
    }

    public record VehiculeInfo(String marque, String modele, String couleur, String immatriculation, int nombrePlaces) {
    }

    public record PointInfo(String nom, double latitude, double longitude) {
    }

    public record PassagerInfo(Long id, String prenom, String nom, String destinationTexte,
            Double destinationLat, Double destinationLng,
            int nombrePlaces, String statut) {
    }

    public record EvaluationInfo(String auteurPrenom, String ciblePrenom, int note, String commentaire, String type,
            LocalDateTime date) {
    }
}
