package com.covoiturage.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/** Transforme les erreurs de validation en reponse JSON lisible (les autres erreurs
 *  passent par le mecanisme standard de ResponseStatusException). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            erreurs.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> corps = new HashMap<>();
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("message", "Donnees invalides");
        corps.put("champs", erreurs);
        return ResponseEntity.badRequest().body(corps);
    }
}
