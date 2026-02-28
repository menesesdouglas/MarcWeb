package com.marcweb.controller;

import com.marcweb.dto.ponto.CreateRegistroPonto;
import com.marcweb.dto.ponto.PontosRecentes;
import com.marcweb.dto.ponto.RegistroPontoResponse;
import com.marcweb.dto.ponto.RegistroPontoResponseWithWarning;
import com.marcweb.dto.ponto.StatusAtualFuncionario;
import com.marcweb.dto.ponto.StatusAtualResponse;
import com.marcweb.dto.TimeAdjustmentRequest;
import com.marcweb.dto.ponto.RegistroPontoMe;
import com.marcweb.service.LogAuditoriaService;
import com.marcweb.service.RegistroPontoService;
import com.marcweb.service.RegistroPontoIndividualService;
import com.marcweb.service.RelatorioService;
import com.marcweb.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ponto")
@Slf4j
public class RegistroPontoController {

    private final RegistroPontoService service;
    private final LogAuditoriaService logAuditoriaService;
    private final UserService userService;
    private final RegistroPontoIndividualService registroPontoIndividualService;
    private final RelatorioService relatorioService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<RegistroPontoResponseWithWarning> registrarPonto(@RequestBody @Valid CreateRegistroPonto data, HttpServletRequest request) {
        // Verificar se o usuário tem registros incompletos de dias anteriores
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        Long userId = userService.findByUsername(userDetails.getUsername()).getId();
        boolean hasIncompletePreviousDay = service.temRegistroIncompletoAnterior(userId);

        // Registrar o ponto
        service.registrarPonto(data, request);

        // Retornar resposta com aviso se necessário
        RegistroPontoResponseWithWarning response = new RegistroPontoResponseWithWarning(
                "Ponto batido com sucesso!!!",
                hasIncompletePreviousDay
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<RegistroPontoMe> meusPontos(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        var pontos = service.buscarPontosRegistrados(start, end);
        return ResponseEntity.ok(pontos);
    }

    /// CARREGANDO HISTÓRICO RECENTE DO USUÁRIO LOGADO
    @GetMapping("/historico")
    public ResponseEntity<List<PontosRecentes>> getStatusRecentes(@RequestParam LocalDate date) {
        var lista = service.getStatusRecentes(date);
        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status-atual")
    public ResponseEntity<List<StatusAtualFuncionario>> getStatusAtualFuncionarios() {
        var lista = service.getStatusAtualFuncionarios();
        return ResponseEntity.ok(lista);
    }

    /// CARREGANDO STATUS ATUAL DO USUÁRIO LOGADO
    @GetMapping("/status-atual-me")
    public ResponseEntity<StatusAtualResponse> getStatusAtualMe() {
        var status = service.getStatusAtual();
        return ResponseEntity.ok(status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ajuste")
    public ResponseEntity<String> ajustarPonto(@RequestBody TimeAdjustmentRequest request) {
        service.ajustarPonto(request);
        return ResponseEntity.ok("Ajuste de ponto realizado com sucesso");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ajustes-admin")
    public ResponseEntity<List<RegistroPontoResponse>> getAjustesAdministrativos(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        log.info("Carregando ajustes administrativos - start: {}, end: {}", start, end);
        try {
            var ajustes = service.getAjustesAdministrativos(start, end);
            log.info("Encontrados {} ajustes administrativos", ajustes.size());
            return ResponseEntity.ok(ajustes);
        } catch (Exception e) {
            log.error("Erro ao carregar ajustes administrativos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    /**
     * Endpoint para gerar comprovante PDF de registro de ponto individual
     * Segue a Portaria 671/2021 - Ministério do Trabalho e Emprego
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<String> gerarComprovantePontoIndividual(@PathVariable Long id) {
        try {
            String pdfBase64 = relatorioService.gerarComprovantePontoIndividual(id);
            return ResponseEntity.ok(pdfBase64);
        } catch (Exception e) {
            log.error("Erro ao gerar comprovante de ponto individual: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao gerar comprovante PDF: " + e.getMessage());
        }
    }
}
