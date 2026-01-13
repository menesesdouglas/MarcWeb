package com.marcweb.controller;

import com.marcweb.exceptions.AssinaturaDigitalException;
import com.marcweb.service.AssinaturaDigitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/assinatura")
public class AssinaturaDigitalController {

    private final AssinaturaDigitalService assinaturaDigitalService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/gerar-chaves")
    public ResponseEntity<String[]> gerarParDeChaves() {
        try {
            String[] chaves = assinaturaDigitalService.gerarParDeChaves();
            return ResponseEntity.ok(chaves);
        } catch (AssinaturaDigitalException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/assinar")
    public ResponseEntity<String> assinarDados(
            @RequestParam String dados,
            @RequestParam String chavePrivadaBase64) {
        try {
            String assinatura = assinaturaDigitalService.assinarDados(dados, chavePrivadaBase64);
            return ResponseEntity.ok(assinatura);
        } catch (AssinaturaDigitalException e) {
            return ResponseEntity.internalServerError().body("Falha ao assinar dados: " + e.getMessage());
        }
    }

    @PostMapping("/verificar")
    public ResponseEntity<Boolean> verificarAssinatura(
            @RequestParam String dados,
            @RequestParam String assinatura,
            @RequestParam String chavePublicaBase64) {
        try {
            boolean valido = assinaturaDigitalService.verificarAssinatura(dados, assinatura, chavePublicaBase64);
            return ResponseEntity.ok(valido);
        } catch (AssinaturaDigitalException e) {
            return ResponseEntity.internalServerError().body(false);
        }
    }
}
