package com.covoiturage.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    boolean existsByTrajetIdAndAuteurIdAndCibleId(Long trajetId, Long auteurId, Long cibleId);

    long countByCibleId(Long cibleId);

    List<Evaluation> findByTrajetId(Long trajetId);

    @Query("SELECT AVG(e.note) FROM Evaluation e WHERE e.cible.id = :cibleId")
    Double moyenneByCibleId(@Param("cibleId") Long cibleId);
}
