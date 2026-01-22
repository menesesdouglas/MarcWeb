package com.marcweb.controller;

import com.marcweb.dto.LogAuditoriaResponse;
import com.marcweb.entity.LogAuditoria;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.service.LogAuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auditoria")
public class LogAuditoriaController {

    private final LogAuditoriaService logAuditoriaService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs")
    public ResponseEntity<Page<LogAuditoriaResponse>> buscarLogsComFiltros(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDateTime inicio,
            @RequestParam(required = false) LocalDateTime fim,
            @RequestParam(required = false) CategoriaLog categoria,
            @RequestParam(required = false) StatusLog status,
            @RequestParam(required = false) String acao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LogAuditoriaResponse> logs = logAuditoriaService.buscarLogsComFiltros(userId, inicio, fim, categoria, status, acao, pageable);
        return ResponseEntity.ok(logs);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs/periodo")
    public ResponseEntity<List<LogAuditoriaResponse>> buscarLogsPorPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
        List<LogAuditoriaResponse> logs = logAuditoriaService.buscarLogsPorPeriodo(inicio, fim);
        return ResponseEntity.ok(logs);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs/categoria")
    public ResponseEntity<List<LogAuditoriaResponse>> buscarLogsPorCategoria(
            @RequestParam CategoriaLog categoria) {
        List<LogAuditoriaResponse> logs = logAuditoriaService.buscarLogsPorCategoria(categoria);
        return ResponseEntity.ok(logs);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs/status")
    public ResponseEntity<List<LogAuditoriaResponse>> buscarLogsPorStatus(
            @RequestParam StatusLog status) {
        List<LogAuditoriaResponse> logs = logAuditoriaService.buscarLogsPorStatus(status);
        return ResponseEntity.ok(logs);
    }
}
