package com.marcweb.controller;

import com.marcweb.dto.DataConfigEmpresa;
import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.repository.ConfigEmpresaRepository;
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

    private final ConfigEmpresaRepository repository;

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping
    public void updateConfigEmpresa(@RequestBody @Valid DataConfigEmpresa data) {
        Long idConfigEmpresa = repository.findFirst().orElseThrow(() -> new RuntimeException("Não existe registro correspondente"))
                .getId();
        ConfigEmpresa configEmpresa = ConfigEmpresa.builder()
                .id(idConfigEmpresa)
                .nomeEmpresa(data.getNomeEmpresa())
                .geolocAtiva(data.getGeolocAtiva())
                .latCentro(data.getLatCentro())
                .longCentro(data.getLongCentro())
                .raioGeoloc(data.getRaioGeoloc())
                .build();
        repository.save(configEmpresa);
    }

    @GetMapping
    public ResponseEntity<DataConfigEmpresa> findConfigsEmpresa() {
        var configEmpresa = repository.findFirst().orElse(null);
        return configEmpresa != null ? ResponseEntity.ok(new DataConfigEmpresa(configEmpresa)) : ResponseEntity.notFound().build();
    }
}
