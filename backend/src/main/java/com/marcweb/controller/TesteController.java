package com.marcweb.controller;

import com.marcweb.dto.ponto.RegistroPontoIndividualDTO;
import com.marcweb.entity.RegistroPonto;
import com.marcweb.service.RegistroPontoIndividualService;
import com.marcweb.service.RegistroPontoService;
import com.marcweb.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/teste")
public class TesteController {

    private final RegistroPontoIndividualService registroPontoIndividualService;
    private final RelatorioService relatorioService;
    private final RegistroPontoService registroPontoService;

    @GetMapping("/registro/{id}")
    public ResponseEntity<RegistroPontoIndividualDTO> buscarRegistroTeste(@PathVariable Long id) {
        try {
            // Busca os dados do registro individual
            RegistroPontoIndividualDTO registro = registroPontoIndividualService.buscarRegistroPontoIndividual(id, 1L);
            return ResponseEntity.ok(registro);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<String> gerarPdfTeste(@PathVariable Long id) {
        try {
            String pdfBase64 = relatorioService.gerarComprovantePontoIndividual(id);
            return ResponseEntity.ok(pdfBase64);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }

}