package com.covoiturage.position;

import com.covoiturage.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "position_conducteur")
@Getter
@Setter
@NoArgsConstructor
public class PositionConducteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "conducteur_id", unique = true)
    private Utilisateur conducteur;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private boolean disponible = false;

    @Column(nullable = false)
    private LocalDateTime derniereMaj;
}
