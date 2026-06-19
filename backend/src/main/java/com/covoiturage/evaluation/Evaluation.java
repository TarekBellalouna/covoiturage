package com.covoiturage.evaluation;

import com.covoiturage.trajet.Trajet;
import com.covoiturage.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
@Getter
@Setter
@NoArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int note;

    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeEvaluation type;

    @Column(nullable = false, updatable = false)
    private LocalDateTime date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cible_id")
    private Utilisateur cible;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trajet_id")
    private Trajet trajet;

    @PrePersist
    void onCreate() {
        this.date = LocalDateTime.now();
    }
}
