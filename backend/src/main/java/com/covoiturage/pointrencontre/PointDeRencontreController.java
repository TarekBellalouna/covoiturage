package com.covoiturage.pointrencontre;

import com.covoiturage.pointrencontre.dto.PointDeRencontreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/points-rencontre")
@RequiredArgsConstructor
public class PointDeRencontreController {

    private final PointDeRencontreRepository points;

    @GetMapping
    public List<PointDeRencontreResponse> lister(@RequestParam(required = false) String ville) {
        List<PointDeRencontre> resultats = (ville == null || ville.isBlank())
                ? points.findAll()
                : points.findByVilleIgnoreCase(ville);
        return resultats.stream().map(PointDeRencontreResponse::from).toList();
    }
}
