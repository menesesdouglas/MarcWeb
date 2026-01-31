package com.marcweb.controller;

import com.marcweb.dto.config_empresa.DataConfigEmpresa;
import com.marcweb.service.ConfigEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/config-empresa")
public class ConfigEmpresaController {

    private final ConfigEmpresaService service;

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping
    public void updateConfigEmpresa(@RequestBody @Valid DataConfigEmpresa data) {
        service.updateConfigEmpresa(data);
    }

    @GetMapping
    public ResponseEntity<DataConfigEmpresa> findConfigsEmpresa() {
        var configEmpresa = service.findConfigsEmpresa();
        return ResponseEntity.ok(configEmpresa);
    }
}
