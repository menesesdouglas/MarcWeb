package com.marcweb.controller;

import com.marcweb.exceptions.CacheException;
import com.marcweb.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService cacheService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invalidate")
    public ResponseEntity<String> invalidateCache(
            @RequestParam String cacheName,
            @RequestParam String key) {
        try {
            cacheService.invalidateCache(cacheName, key);
            return ResponseEntity.ok("Cache invalidado com sucesso");
        } catch (CacheException e) {
            return ResponseEntity.internalServerError().body("Falha ao invalidar cache: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invalidate-folha-ponto")
    public ResponseEntity<String> invalidateFolhaPontoCache(
            @RequestParam Long userId,
            @RequestParam int mes,
            @RequestParam int ano) {
        try {
            cacheService.invalidateFolhaPontoCache(userId, mes, ano);
            return ResponseEntity.ok("Cache de folha de ponto invalidado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Falha ao invalidar cache: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invalidate-config-empresa")
    public ResponseEntity<String> invalidateConfigEmpresaCache() {
        try {
            cacheService.invalidateConfigEmpresaCache();
            return ResponseEntity.ok("Cache de configuração da empresa invalidado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Falha ao invalidar cache: " + e.getMessage());
        }
    }
}
