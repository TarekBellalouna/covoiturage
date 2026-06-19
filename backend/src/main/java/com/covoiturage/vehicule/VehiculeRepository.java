package com.covoiturage.vehicule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    List<Vehicule> findByProprietaireId(Long proprietaireId);

    long countByProprietaireId(Long proprietaireId);
}
