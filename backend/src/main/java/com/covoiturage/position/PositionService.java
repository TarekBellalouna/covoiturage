package com.covoiturage.position;

import com.covoiturage.position.dto.PositionRequest;
import com.covoiturage.user.Utilisateur;
import com.covoiturage.user.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionConducteurRepository positions;
    private final UtilisateurRepository utilisateurs;

    @Transactional
    public void mettreAJour(Long conducteurId, PositionRequest req) {
        Utilisateur u = utilisateurs.findById(conducteurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!u.isEstConducteur()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reserve aux conducteurs");
        }

        PositionConducteur p = positions.findByConducteurId(conducteurId)
                .orElseGet(() -> {
                    PositionConducteur nouvelle = new PositionConducteur();
                    nouvelle.setConducteur(u);
                    return nouvelle;
                });
        p.setLatitude(req.latitude());
        p.setLongitude(req.longitude());
        p.setDisponible(req.disponible());
        p.setDerniereMaj(LocalDateTime.now());
        positions.save(p);
    }
}
