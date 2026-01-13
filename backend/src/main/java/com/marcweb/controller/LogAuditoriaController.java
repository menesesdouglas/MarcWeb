package com.marcweb.controller;

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
    public ResponseEntity<List<LogAuditoria>> buscarLogsPorUsuario(@RequestParam Long userId) {
        List<LogAuditoria> logs = logAuditoriaService.buscarLogsPorUsuario(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/periodo")
    public ResponseEntity<List<LogAuditoria>> buscarLogsPorPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
        List<LogAuditoria> logs = logAuditoriaService.buscarLogsPorPeriodo(inicio, fim);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/categoria")
    public ResponseEntity<List<LogAuditoria>> buscarLogsPorCategoria(
            @RequestParam CategoriaLog categoria) {
        List<LogAuditoria> logs = logAuditoriaService.buscarLogsPorCategoria(categoria);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/status")
    public ResponseEntity<List<LogAuditoria>> buscarLogsPorStatus(
            @RequestParam StatusLog status) {
        List<LogAuditoria> logs = logAuditoriaService.buscarLogsPorStatus(status);
        return ResponseEntity.ok(logs);
    }
}
