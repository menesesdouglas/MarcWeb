package com.marcweb.controller;

import com.marcweb.exceptions.AEJException;
import com.marcweb.service.AEJService;
import com.marcweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/aej")
public class AEJController {

    private final AEJService aejService;
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/gerar")
    public ResponseEntity<String> gerarAEJ(
            @RequestParam String folhaPontoJson,
            @RequestParam String chavePrivadaBase64,
            @RequestParam Long userId) {
        try {
            var user = userService.findById(userId);
            String aej = aejService.gerarAEJ(folhaPontoJson, user, chavePrivadaBase64);
            return ResponseEntity.ok(aej);
        } catch (AEJException e) {
            return ResponseEntity.internalServerError().body("Falha ao gerar AEJ: " + e.getMessage());
        }
    }

    @PostMapping("/validar")
    public ResponseEntity<Boolean> validarAEJ(
            @RequestParam String aejJson,
            @RequestParam String chavePublicaBase64) {
        try {
            boolean valido = aejService.validarAEJ(aejJson, chavePublicaBase64);
            return ResponseEntity.ok(valido);
        } catch (AEJException e) {
            return ResponseEntity.internalServerError().body(false);
        }
    }
}
