package com.covoiturage.position;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PositionConducteurRepository extends JpaRepository<PositionConducteur, Long> {

    Optional<PositionConducteur> findByConducteurId(Long conducteurId);

    /**
     * Renvoie les identifiants des conducteurs disponibles situes dans un rayon
     * (en metres) autour d'un point, via la fonction PostGIS ST_DWithin.
     */
    @Query(value = """
            SELECT p.conducteur_id
            FROM position_conducteur p
            WHERE p.disponible = true
              AND ST_DWithin(
                    ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326)::geography,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                    :rayonMetres)
            """, nativeQuery = true)
    List<Long> trouverConducteursProches(@Param("lat") double lat,
                                         @Param("lng") double lng,
                                         @Param("rayonMetres") double rayonMetres);
}
