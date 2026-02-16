package com.marcweb.controller;

import com.marcweb.dto.tenant.TenantResponse;
import com.marcweb.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/tenant")
@RestController
public class TenantController {

    private final TenantService service;

    @GetMapping
    public ResponseEntity<List<TenantResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}
