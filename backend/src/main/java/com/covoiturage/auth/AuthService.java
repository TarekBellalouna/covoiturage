package com.covoiturage.auth;

import com.covoiturage.auth.dto.AuthResponse;
import com.covoiturage.auth.dto.LoginRequest;
import com.covoiturage.auth.dto.RegisterRequest;
import com.covoiturage.security.JwtService;
import com.covoiturage.user.ModeUtilisateur;
import com.covoiturage.user.Utilisateur;
import com.covoiturage.user.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurs,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.utilisateurs = utilisateurs;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        if (utilisateurs.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est deja utilise");
        }

        Utilisateur u = new Utilisateur();
        u.setNom(req.nom());
        u.setPrenom(req.prenom());
        u.setEmail(req.email());
        u.setMotDePasse(passwordEncoder.encode(req.motDePasse()));
        u.setTelephone(req.telephone());
        u.setEstConducteur(false);
        u.setModeActif(ModeUtilisateur.PASSAGER);

        utilisateurs.save(u);

        return buildResponse(u);
    }

    public AuthResponse login(LoginRequest req) {
        Utilisateur u = utilisateurs.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));

        if (!passwordEncoder.matches(req.motDePasse(), u.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }

        return buildResponse(u);
    }

    private AuthResponse buildResponse(Utilisateur u) {
        String token = jwtService.generateToken(u.getId(), u.getEmail());
        return new AuthResponse(token, u.getId(), u.getEmail(), u.getModeActif().name());
    }
}
