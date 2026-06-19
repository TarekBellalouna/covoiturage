package com.covoiturage.position;

import com.covoiturage.position.dto.PositionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conducteurs/me")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /** Remontee periodique de la position du conducteur (toutes les ~10-15 s cote client). */
    @PatchMapping("/position")
    public void mettreAJour(@AuthenticationPrincipal String userId,
                            @Valid @RequestBody PositionRequest request) {
        positionService.mettreAJour(Long.valueOf(userId), request);
    }
}
