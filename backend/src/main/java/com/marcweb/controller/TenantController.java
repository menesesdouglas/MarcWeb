package com.marcweb.controller;

import com.marcweb.dto.tenant.CreateTenantRequest;
import com.marcweb.dto.tenant.TenantResponse;
import com.marcweb.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/tenant")
@RestController
public class TenantController {

    private final TenantService service;

    /// CHAVE DE AMIN PARA CRIAÇÃO DE TENANTS
    private static final String ADMIN_KEY = "marcweb_admin_2026";

    @GetMapping
    public ResponseEntity<List<TenantResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTenant(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @Valid @RequestBody CreateTenantRequest request) {

        // Verificar a chave de admin
        if (adminKey == null || !adminKey.equals(ADMIN_KEY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso negado. Chave de admin inválida."));
        }

        try {
            TenantResponse tenant = service.createTenant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao criar tenant: " + e.getMessage()));
        }
    }
}
