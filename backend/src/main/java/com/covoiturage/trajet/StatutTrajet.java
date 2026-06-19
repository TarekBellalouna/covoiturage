package com.covoiturage.trajet;

public enum StatutTrajet {
    OUVERT,     // accepte encore des passagers tant qu'il reste des places
    EN_COURS,   // le conducteur a demarre
    TERMINE,
    ANNULE
}
