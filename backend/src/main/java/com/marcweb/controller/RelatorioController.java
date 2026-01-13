package com.marcweb.controller;

import com.marcweb.exceptions.RelatorioException;
import com.marcweb.service.RelatorioService;
import com.marcweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pdf")
    public ResponseEntity<String> gerarRelatorioPDF(
            @RequestParam String folhaPontoJson,
            @RequestParam Long userId) {
        try {
            var user = userService.findById(userId);
            String pdfBase64 = relatorioService.gerarRelatorioPDF(folhaPontoJson, user);
            return ResponseEntity.ok(pdfBase64);
        } catch (RelatorioException e) {
            return ResponseEntity.internalServerError().body("Falha ao gerar relatório PDF: " + e.getMessage());
        }
    }
}
