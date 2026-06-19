package com.covoiturage.notification;

import com.covoiturage.demande.DemandeCovoiturage;
import com.covoiturage.demande.dto.DemandeResponse;
import com.covoiturage.notification.dto.NotificationResponse;
import com.covoiturage.user.Utilisateur;
import com.covoiturage.user.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gere le temps reel (push WebSocket) et les notifications persistees.
 * - conducteurs : nouvelles demandes, sans filtre de distance
 * - passager : acceptation et changements d'etat de son trajet
 * - centre de notifications : liste + compteur de non-lues
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messaging;
    private final NotificationRepository notifications;
    private final UtilisateurRepository utilisateurs;

    public void notifierTousLesConducteurs(DemandeCovoiturage demande) {
        DemandeResponse payload = DemandeResponse.from(demande);
        Long passagerId = demande.getPassager().getId();

        for (Utilisateur conducteur : utilisateurs.findByEstConducteurTrue()) {
            Long conducteurId = conducteur.getId();
            if (conducteurId.equals(passagerId)) {
                continue;
            }

            messaging.convertAndSendToUser(String.valueOf(conducteurId), "/queue/demandes", payload);
            creer(conducteurId, TypeNotification.DEMANDE_RECUE,
                    "Nouvelle demande de covoiturage disponible", null, demande.getId());
        }
    }

    public void notifierPassager(DemandeCovoiturage demande) {
        Long passagerId = demande.getPassager().getId();
        messaging.convertAndSendToUser(String.valueOf(passagerId), "/queue/suivi", DemandeResponse.from(demande));

        Long trajetId = demande.getTrajet() != null ? demande.getTrajet().getId() : null;
        switch (demande.getStatut()) {
            case ACCEPTEE -> creer(passagerId, TypeNotification.DEMANDE_ACCEPTEE,
                    "Votre demande a ete acceptee", trajetId, demande.getId());
            case EN_COURS -> creer(passagerId, TypeNotification.TRAJET_DEMARRE,
                    "Votre trajet a demarre", trajetId, demande.getId());
            case TERMINEE -> creer(passagerId, TypeNotification.TRAJET_TERMINE,
                    "Votre trajet est termine", trajetId, demande.getId());
            case ANNULEE -> creer(passagerId, TypeNotification.TRAJET_ANNULE,
                    "Votre trajet a ete annule", trajetId, demande.getId());
            default -> {
            }
        }
    }

    private void creer(Long destinataireId, TypeNotification type, String message, Long trajetId, Long demandeId) {
        Notification n = new Notification();
        n.setDestinataire(utilisateurs.getReferenceById(destinataireId));
        n.setType(type);
        n.setMessage(message);
        n.setTrajetId(trajetId);
        n.setDemandeId(demandeId);
        notifications.save(n);
        messaging.convertAndSendToUser(String.valueOf(destinataireId), "/queue/notifications",
                NotificationResponse.from(n));
    }

    // --- Centre de notifications ---

    public List<NotificationResponse> lister(Long userId) {
        return notifications.findTop50ByDestinataireIdOrderByDateCreationDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long compterNonLues(Long userId) {
        return notifications.countByDestinataireIdAndLuFalse(userId);
    }

    @Transactional
    public void marquerToutLu(Long userId) {
        notifications.marquerToutLu(userId);
    }
}
