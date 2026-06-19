package com.covoiturage.trajet;

import com.covoiturage.demande.DemandeCovoiturage;
import com.covoiturage.pointrencontre.PointDeRencontre;
import com.covoiturage.user.Utilisateur;
import com.covoiturage.vehicule.Vehicule;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trajets")
@Getter
@Setter
@NoArgsConstructor
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conducteur_id")
    private Utilisateur conducteur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "point_depart_id")
    private PointDeRencontre pointDepart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTrajet statut = StatutTrajet.OUVERT;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @OneToMany(mappedBy = "trajet")
    private List<DemandeCovoiturage> demandes = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
