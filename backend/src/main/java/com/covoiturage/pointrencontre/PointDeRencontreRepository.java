package com.covoiturage.pointrencontre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointDeRencontreRepository extends JpaRepository<PointDeRencontre, Long> {

    List<PointDeRencontre> findByVilleIgnoreCase(String ville);
}
