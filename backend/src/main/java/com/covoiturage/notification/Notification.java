package com.covoiturage.notification;

import com.covoiturage.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destinataire_id")
    private Utilisateur destinataire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean lu = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private Long trajetId;

    private Long demandeId;

    @PrePersist
    void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
