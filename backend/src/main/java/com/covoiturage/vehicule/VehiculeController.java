package com.covoiturage.vehicule;

import com.covoiturage.vehicule.dto.VehiculeRequest;
import com.covoiturage.vehicule.dto.VehiculeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @GetMapping
    public List<VehiculeResponse> mesVehicules(@AuthenticationPrincipal String userId) {
        return vehiculeService.mesVehicules(Long.valueOf(userId));
    }

    @PostMapping
    public VehiculeResponse ajouter(@AuthenticationPrincipal String userId,
                                    @Valid @RequestBody VehiculeRequest request) {
        return vehiculeService.ajouter(Long.valueOf(userId), request);
    }

    @PatchMapping("/{id}")
    public VehiculeResponse modifier(@AuthenticationPrincipal String userId,
                                     @PathVariable Long id,
                                     @Valid @RequestBody VehiculeRequest request) {
        return vehiculeService.modifier(Long.valueOf(userId), id, request);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@AuthenticationPrincipal String userId, @PathVariable Long id) {
        vehiculeService.supprimer(Long.valueOf(userId), id);
    }
}
