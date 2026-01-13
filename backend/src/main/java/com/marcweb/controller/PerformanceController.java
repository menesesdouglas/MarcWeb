package com.marcweb.controller;

import com.marcweb.entity.User;
import com.marcweb.exceptions.PerformanceException;
import com.marcweb.service.PerformanceService;
import com.marcweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/otimizar")
    public ResponseEntity<Map<String, Object>> otimizarPerformance() {
        try {
            Map<String, Object> resultado = performanceService.otimizarPerformance();
            return ResponseEntity.ok(resultado);
        } catch (PerformanceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/monitorar")
    public ResponseEntity<Map<String, Object>> monitorarPerformance() {
        try {
            Map<String, Object> metrics = performanceService.monitorarPerformance();
            return ResponseEntity.ok(metrics);
        } catch (PerformanceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/otimizar-consultas")
    public ResponseEntity<Map<String, Object>> otimizarConsultas() {
        try {
            UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
            User user = userService.findByUsername(userDetails.getUsername());
            Map<String, Object> resultado = performanceService.otimizarConsultas(user);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
