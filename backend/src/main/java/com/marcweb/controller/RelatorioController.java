package com.marcweb.controller;

import com.marcweb.exceptions.RelatorioException;
import com.marcweb.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pdf")
    public ResponseEntity<String> gerarRelatorioPDF(
            @RequestBody RelatorioRequest request) {
        try {
            String pdfBase64 = relatorioService.gerarRelatorioPDF(request.getUserId(), request.getMes(), request.getAno());
            return ResponseEntity.ok(pdfBase64);
        } catch (RelatorioException e) {
            return ResponseEntity.internalServerError().body("Falha ao gerar relatório PDF: " + e.getMessage());
        }
    }

    // Classe interna para o request body
    public static class RelatorioRequest {
        private Long userId;
        private int mes;
        private int ano;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public int getMes() { return mes; }
        public void setMes(int mes) { this.mes = mes; }
        public int getAno() { return ano; }
        public void setAno(int ano) { this.ano = ano; }
    }
}
