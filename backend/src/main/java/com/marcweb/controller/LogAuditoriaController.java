package com.marcweb.controller;

import com.marcweb.dto.LogAuditoriaResponse;
import com.marcweb.entity.LogAuditoria;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.service.LogAuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auditoria")
public class LogAuditoriaController {

    private final LogAuditoriaService logAuditoriaService;

    @GetMapping("/logs")
    public ResponseEntity<List<LogAuditoriaResponse>> buscarLogsPorUsuario(@RequestParam Long userId) {
        List<LogAuditoriaResponse> logs = logAuditoriaService.buscarLogsPorUsuario(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/periodo")
    public ResponseEntity<List<LogAuditoriaResponse>> buscarLogsPorPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
        List<LogAuditoriaResponse> logs = logAuditoriaService.buscarLogsPorPeriodo(inicio, fim);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/categoria")
    public ResponseEntity<List<LogAuditoriaResponse>> buscarLogsPorCategoria(
            @RequestParam CategoriaLog categoria) {
        List<LogAuditoriaResponse> logs = logAuditoriaService.buscarLogsPorCategoria(categoria);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/status")
    public ResponseEntity<List<LogAuditoriaResponse>> buscarLogsPorStatus(
            @RequestParam StatusLog status) {
        List<LogAuditoriaResponse> logs = logAuditoriaService.buscarLogsPorStatus(status);
        return ResponseEntity.ok(logs);
    }
}
