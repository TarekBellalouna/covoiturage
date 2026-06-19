package com.covoiturage.demande;

import com.covoiturage.pointrencontre.PointDeRencontre;
import com.covoiturage.trajet.Trajet;
import com.covoiturage.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "demandes")
@Getter
@Setter
@NoArgsConstructor
public class DemandeCovoiturage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "passager_id")
    private Utilisateur passager;

    @ManyToOne(optional = false)
    @JoinColumn(name = "point_rencontre_id")
    private PointDeRencontre pointRencontre;

    @Column(nullable = false)
    private String destinationTexte;

    @Column(nullable = false)
    private Double destinationLat;

    private Double prix;

    @Column(nullable = false)
    private Double destinationLng;

    @Column(nullable = false)
    private int nombrePlacesDemandees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateExpiration;

    /**
     * Renseigne une fois la demande acceptee : le trajet auquel elle est rattachee.
     */
    @ManyToOne
    @JoinColumn(name = "trajet_id")
    private Trajet trajet;

    /**
     * Verrou optimiste : protege la transition EN_ATTENTE -> ACCEPTEE contre les
     * acceptations concurrentes.
     */
    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
