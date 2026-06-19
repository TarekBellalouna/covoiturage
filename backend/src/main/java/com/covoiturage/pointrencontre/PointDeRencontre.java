package com.covoiturage.pointrencontre;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "points_rencontre")
@Getter
@Setter
@NoArgsConstructor
public class PointDeRencontre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private String ville;

    public PointDeRencontre(String nom, double latitude, double longitude, String ville) {
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ville = ville;
    }
}
