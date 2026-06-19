package com.covoiturage.trajet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    List<Trajet> findByConducteurIdOrderByDateCreationDesc(Long conducteurId);

    List<Trajet> findByStatutIn(List<StatutTrajet> statuts);

    long countByConducteurIdAndStatut(Long conducteurId, StatutTrajet statut);
}
