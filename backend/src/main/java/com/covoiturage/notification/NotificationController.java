package com.covoiturage.notification;

import com.covoiturage.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> lister(@AuthenticationPrincipal String userId) {
        return notificationService.lister(Long.valueOf(userId));
    }

    @GetMapping("/non-lues")
    public Map<String, Long> compterNonLues(@AuthenticationPrincipal String userId) {
        return Map.of("nombre", notificationService.compterNonLues(Long.valueOf(userId)));
    }

    @PostMapping("/lu-tout")
    public void marquerToutLu(@AuthenticationPrincipal String userId) {
        notificationService.marquerToutLu(Long.valueOf(userId));
    }
}
