package com.covoiturage.user;

import com.covoiturage.demande.DemandeRepository;
import com.covoiturage.demande.StatutDemande;
import com.covoiturage.evaluation.EvaluationRepository;
import com.covoiturage.trajet.StatutTrajet;
import com.covoiturage.trajet.TrajetRepository;
import com.covoiturage.user.dto.PublicProfileResponse;
import com.covoiturage.user.dto.UpdateModeRequest;
import com.covoiturage.user.dto.UpdateProfileRequest;
import com.covoiturage.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UtilisateurRepository utilisateurs;
    private final EvaluationRepository evaluations;
    private final TrajetRepository trajets;
    private final DemandeRepository demandes;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal String userId) {
        return toResponse(charger(userId));
    }

    @PatchMapping("/me/mode")
    @Transactional
    public UserResponse changerMode(@AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateModeRequest request) {
        Utilisateur u = charger(userId);
        u.setModeActif(request.mode()); // plus de vérification estConducteur
        return toResponse(u);
    }

    /** Profil public d'un utilisateur (reputation + nombre de trajets). */
    @GetMapping("/{id}")
    public PublicProfileResponse profil(@PathVariable Long id) {
        Utilisateur u = utilisateurs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        Double moyenne = evaluations.moyenneByCibleId(id);
        long nombreAvis = evaluations.countByCibleId(id);
        long trajetsConducteur = trajets.countByConducteurIdAndStatut(id, StatutTrajet.TERMINE);
        long trajetsPassager = demandes.countByPassagerIdAndStatut(id, StatutDemande.TERMINEE);
        return new PublicProfileResponse(
                u.getId(), u.getPrenom(), u.getNom(), u.getPhotoUrl(), u.getTelephone(),
                u.isEstConducteur(), moyenne != null ? moyenne : 0.0, nombreAvis,
                trajetsConducteur, trajetsPassager);
    }

    /** Modification du profil (nom, prenom, telephone, photo). */
    @PatchMapping("/me/profil")
    @Transactional
    public UserResponse modifierProfil(@AuthenticationPrincipal String userId,
            @RequestBody UpdateProfileRequest request) {
        Utilisateur u = charger(userId);
        if (request.nom() != null && !request.nom().isBlank())
            u.setNom(request.nom());
        if (request.prenom() != null && !request.prenom().isBlank())
            u.setPrenom(request.prenom());
        if (request.telephone() != null)
            u.setTelephone(request.telephone());
        if (request.photoUrl() != null)
            u.setPhotoUrl(request.photoUrl());
        return toResponse(u);
    }

    private Utilisateur charger(String userId) {
        return utilisateurs.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private UserResponse toResponse(Utilisateur u) {
        return new UserResponse(
                u.getId(), u.getNom(), u.getPrenom(), u.getEmail(),
                u.getTelephone(), u.getPhotoUrl(), u.isEstConducteur(),
                u.getModeActif().name(), u.getNumeroPermis());
    }
}
