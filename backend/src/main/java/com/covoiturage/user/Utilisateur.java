package com.covoiturage.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs",
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    /** Hash BCrypt, jamais le mot de passe en clair. */
    @Column(nullable = false)
    private String motDePasse;

    private String telephone;

    /** Photo de profil stockee en data URL (base64). */
    @Column(columnDefinition = "TEXT")
    private String photoUrl;

    /** Passe a true quand l'utilisateur enregistre son premier vehicule. */
    @Column(nullable = false)
    private boolean estConducteur = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModeUtilisateur modeActif = ModeUtilisateur.PASSAGER;

    private String numeroPermis;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateInscription;

    @PrePersist
    void onCreate() {
        this.dateInscription = LocalDateTime.now();
    }
}
