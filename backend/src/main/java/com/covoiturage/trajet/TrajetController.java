package com.covoiturage.trajet;

import com.covoiturage.trajet.dto.TrajetDetailResponse;
import com.covoiturage.trajet.dto.TrajetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;

    @GetMapping("/{id}")
    public TrajetResponse detail(@PathVariable Long id) {
        return trajetService.detail(id);
    }

    @GetMapping("/{id}/details")
    public TrajetDetailResponse details(@AuthenticationPrincipal String userId, @PathVariable Long id) {
        return trajetService.details(Long.valueOf(userId), id);
    }

    @GetMapping("/me")
    public List<TrajetResponse> mesTrajets(@AuthenticationPrincipal String userId) {
        return trajetService.mesTrajets(Long.valueOf(userId));
    }

    @PostMapping("/{id}/demarrer")
    public TrajetResponse demarrer(@AuthenticationPrincipal String userId, @PathVariable Long id) {
        return trajetService.demarrer(Long.valueOf(userId), id);
    }

    @PostMapping("/{id}/terminer")
    public TrajetResponse terminer(@AuthenticationPrincipal String userId, @PathVariable Long id) {
        return trajetService.terminer(Long.valueOf(userId), id);
    }

    @PostMapping("/{id}/annuler")
    public TrajetResponse annuler(@AuthenticationPrincipal String userId, @PathVariable Long id) {
        return trajetService.annuler(Long.valueOf(userId), id);
    }
}
