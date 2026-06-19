package com.covoiturage.demande;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DemandeRepository extends JpaRepository<DemandeCovoiturage, Long> {

       List<DemandeCovoiturage> findByPassagerIdOrderByDateCreationDesc(Long passagerId);

       List<DemandeCovoiturage> findByStatut(StatutDemande statut);

       long countByPassagerIdAndStatut(Long passagerId, StatutDemande statut);

       /** Somme des places deja rattachees a un trajet (hors demandes annulees). */
       @Query("""
                     SELECT COALESCE(SUM(d.nombrePlacesDemandees), 0)
                     FROM DemandeCovoiturage d
                     WHERE d.trajet.id = :trajetId AND d.statut <> :exclu
                     """)
       long placesOccupees(@Param("trajetId") Long trajetId, @Param("exclu") StatutDemande exclu);

       /** Expire en masse les demandes restees trop longtemps en attente. */
       @Modifying
       @Query("""
                     UPDATE DemandeCovoiturage d
                     SET d.statut = com.covoiturage.demande.StatutDemande.EXPIREE
                     WHERE d.statut = com.covoiturage.demande.StatutDemande.EN_ATTENTE
                       AND d.dateExpiration < :maintenant
                     """)
       int expirerDemandesDepassees(@Param("maintenant") LocalDateTime maintenant);

       /**
        * Demandes EN_ATTENTE dont le point de rencontre est dans un rayon (metres)
        * autour d'une position donnee. Exclut les demandes du conducteur lui-meme.
        */
       @Query(value = """
                     SELECT d.*
                     FROM demandes d
                     JOIN points_rencontre pr ON pr.id = d.point_rencontre_id
                     WHERE d.statut = 'EN_ATTENTE'
                       AND d.passager_id <> :conducteurId
                       AND ST_DWithin(
                             ST_SetSRID(ST_MakePoint(pr.longitude, pr.latitude), 4326)::geography,
                             ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                             :rayonMetres)
                     ORDER BY d.date_creation DESC
                     """, nativeQuery = true)
       List<DemandeCovoiturage> trouverDemandesProches(@Param("conducteurId") Long conducteurId,
                     @Param("lat") double lat,
                     @Param("lng") double lng,
                     @Param("rayonMetres") double rayonMetres);
}
