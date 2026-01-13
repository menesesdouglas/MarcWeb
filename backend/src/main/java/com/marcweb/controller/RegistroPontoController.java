package com.marcweb.controller;


import com.marcweb.dto.CreateRegistroPonto;
import com.marcweb.dto.RegistroPontoResponse;
import com.marcweb.service.LogAuditoriaService;
import com.marcweb.service.RegistroPontoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ponto")
public class RegistroPontoController {


    private final RegistroPontoService service;
    private final LogAuditoriaService logAuditoriaService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String registrarPonto(@RequestBody @Valid CreateRegistroPonto data, HttpServletRequest request) {
        service.registrarPonto(data, request);
        return "Ponto batido com sucesso!!!";
    }


    @GetMapping("/me")
    public ResponseEntity<List<RegistroPontoResponse>> meusPontos(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
            ) {
        var pontos = service.buscarPontosRegistrados(start, end);
        return !pontos.isEmpty() ? ResponseEntity.ok(pontos) : ResponseEntity.notFound().build();
    }
}
