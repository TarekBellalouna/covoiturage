package com.covoiturage.demande;

import com.covoiturage.demande.dto.AccepterRequest;
import com.covoiturage.demande.dto.DemandeRequest;
import com.covoiturage.demande.dto.DemandeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService;

    /** Passager : cree une demande (declenche le push temps reel vers les conducteurs proches). */
    @PostMapping
    public DemandeResponse creer(@AuthenticationPrincipal String userId,
                                 @Valid @RequestBody DemandeRequest request) {
        return demandeService.creer(Long.valueOf(userId), request);
    }

    @GetMapping("/{id}")
    public DemandeResponse detail(@PathVariable Long id) {
        return demandeService.detail(id);
    }

    /** Passager : historique de ses demandes. */
    @GetMapping("/me")
    public List<DemandeResponse> mesDemandes(@AuthenticationPrincipal String userId) {
        return demandeService.mesDemandes(Long.valueOf(userId));
    }

    /** Conducteur : demandes en attente (chargement initial de l'ecran). */
    @GetMapping("/proximite")
    public List<DemandeResponse> enAttente(@AuthenticationPrincipal String userId) {
        return demandeService.enAttente(Long.valueOf(userId));
    }

    @PostMapping("/{id}/annuler")
    public void annuler(@AuthenticationPrincipal String userId, @PathVariable Long id) {
        demandeService.annuler(Long.valueOf(userId), id);
    }

    /** Conducteur : accepte une demande. */
    @PostMapping("/{id}/accepter")
    public DemandeResponse accepter(@AuthenticationPrincipal String userId,
                                    @PathVariable Long id,
                                    @Valid @RequestBody AccepterRequest request) {
        return demandeService.accepter(Long.valueOf(userId), id, request);
    }
}
