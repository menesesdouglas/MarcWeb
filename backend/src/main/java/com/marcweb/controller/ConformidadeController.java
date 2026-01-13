package com.marcweb.controller;

import com.marcweb.exceptions.ConformidadeException;
import com.marcweb.service.ConformidadeService;
import com.marcweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/conformidade")
public class ConformidadeController {

    private final ConformidadeService conformidadeService;
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/validar-folha-ponto")
    public ResponseEntity<Map<String, Object>> validarConformidadeFolhaPonto(
            @RequestParam Long userId,
            @RequestParam int mes,
            @RequestParam int ano) {
        try {
            Map<String, Object> resultado = conformidadeService.validarConformidadeFolhaPonto(userId, mes, ano);
            return ResponseEntity.ok(resultado);
        } catch (ConformidadeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/gerar-aej")
    public ResponseEntity<String> gerarEASSinar(
            @RequestParam Long userId,
            @RequestParam int mes,
            @RequestParam int ano,
            @RequestParam String chavePrivada) {
        try {
            UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
            var user = userService.findByUsername(userDetails.getUsername());
            String aejAssinado = conformidadeService.gerarEASSinar(userId, mes, ano, user, chavePrivada);
            return ResponseEntity.ok(aejAssinado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/validar-assinatura")
    public ResponseEntity<Boolean> validarAssinaturaAEJ(
            @RequestParam String aejJson,
            @RequestParam String chavePublica) {
        try {
            boolean valido = conformidadeService.validarAssinaturaAEJ(aejJson, chavePublica);
            return ResponseEntity.ok(valido);
        } catch (ConformidadeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/validar-portaria-671")
    public ResponseEntity<Map<String, Object>> validarConformidadeComPortaria671(
            @RequestParam Long userId,
            @RequestParam int mes,
            @RequestParam int ano) {
        try {
            Map<String, Object> resultado = conformidadeService.validarConformidadeComPortaria671(userId, mes, ano);
            return ResponseEntity.ok(resultado);
        } catch (ConformidadeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
