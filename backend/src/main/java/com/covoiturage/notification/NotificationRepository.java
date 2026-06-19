package com.covoiturage.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByDestinataireIdOrderByDateCreationDesc(Long destinataireId);

    long countByDestinataireIdAndLuFalse(Long destinataireId);

    @Modifying
    @Query("UPDATE Notification n SET n.lu = true WHERE n.destinataire.id = :id AND n.lu = false")
    int marquerToutLu(@Param("id") Long id);
}
