package com.covoiturage.common;

import com.covoiturage.pointrencontre.PointDeRencontre;
import com.covoiturage.pointrencontre.PointDeRencontreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Insere quelques points de rencontre au demarrage si la table est vide. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PointDeRencontreRepository points;

    @Override
    public void run(String... args) {
        if (points.count() > 0) {
            return;
        }
        points.saveAll(List.of(
        // --- Paris ---
        new PointDeRencontre("Gare du Nord", 48.8809, 2.3553, "Paris"),
        new PointDeRencontre("Gare de l'Est", 48.8767, 2.3590, "Paris"),
        new PointDeRencontre("Gare de Lyon", 48.8443, 2.3743, "Paris"),
        new PointDeRencontre("Gare Montparnasse", 48.8410, 2.3209, "Paris"),
        new PointDeRencontre("Gare Saint-Lazare", 48.8757, 2.3253, "Paris"),
        new PointDeRencontre("Châtelet - Les Halles", 48.8616, 2.3470, "Paris"),
        new PointDeRencontre("La Défense", 48.8918, 2.2389, "Paris"),
        // --- Reims ---
        new PointDeRencontre("Gare de Reims Centre", 49.2585, 4.0250, "Reims"),
        new PointDeRencontre("Reims Champagne-Ardenne TGV", 49.2166, 3.9926, "Reims"),
        new PointDeRencontre("Place Drouet d'Erlon", 49.2530, 4.0290, "Reims"),
        new PointDeRencontre("Cathédrale Notre-Dame", 49.2536, 4.0345, "Reims"),
        new PointDeRencontre("Campus Croix-Rouge (URCA)", 49.2356, 4.0436, "Reims"),
        // --- Autres villes ---
        new PointDeRencontre("Lyon Part-Dieu", 45.7605, 4.8597, "Lyon"),
        new PointDeRencontre("Marseille Saint-Charles", 43.3027, 5.3803, "Marseille"),
        new PointDeRencontre("Lille Flandres", 50.6365, 3.0708, "Lille"),
        new PointDeRencontre("Bordeaux Saint-Jean", 44.8259, -0.5562, "Bordeaux"),
        new PointDeRencontre("Toulouse Matabiau", 43.6111, 1.4536, "Toulouse"),
        new PointDeRencontre("Nantes Gare", 47.2173, -1.5419, "Nantes"),
        new PointDeRencontre("Strasbourg Gare", 48.5851, 7.7341, "Strasbourg"),
        new PointDeRencontre("Nice-Ville", 43.7045, 7.2619, "Nice"),
        new PointDeRencontre("Rennes Gare", 48.1035, -1.6724, "Rennes"),
        new PointDeRencontre("Montpellier Saint-Roch", 43.6045, 3.8807, "Montpellier"),
        new PointDeRencontre("Dijon-Ville", 47.3236, 5.0273, "Dijon"),
        new PointDeRencontre("Metz-Ville", 49.1097, 6.1776, "Metz"),
        new PointDeRencontre("Nancy-Ville", 48.6896, 6.1742, "Nancy")
));
    }
}
