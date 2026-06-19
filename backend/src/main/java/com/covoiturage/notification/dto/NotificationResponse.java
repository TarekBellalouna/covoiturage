package com.covoiturage.notification.dto;

import com.covoiturage.notification.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String message,
        boolean lu,
        LocalDateTime dateCreation,
        Long trajetId,
        Long demandeId
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType().name(), n.getMessage(), n.isLu(),
                n.getDateCreation(), n.getTrajetId(), n.getDemandeId());
    }
}
