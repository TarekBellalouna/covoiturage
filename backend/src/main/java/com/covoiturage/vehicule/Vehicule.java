package com.covoiturage.vehicule;

import com.covoiturage.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicules")
@Getter
@Setter
@NoArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    private String couleur;

    private String immatriculation;

    @Column(nullable = false)
    private int nombrePlaces;

    @Column(nullable = false)
    private boolean actif = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proprietaire_id")
    private Utilisateur proprietaire;
}
