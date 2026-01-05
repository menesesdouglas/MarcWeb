package com.marcweb.controller;

import com.marcweb.dto.CreateFeriado;
import com.marcweb.entity.Feriado;
import com.marcweb.service.BrasilApiService;
import com.marcweb.service.FeriadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/feriado")
public class FeriadoController {

    private final BrasilApiService brasilApiService;

    private final FeriadoService service;


    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/nacionais")
    public void cadastrarFeriadosViaAPI() {
        brasilApiService.cadastrarFeriadosNacionais();
    }


    @GetMapping
    public ResponseEntity<List<Feriado>> buscarFeriadosPorPeriodo(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        var feriados = service.findByDataBetween(start, end);
        return !feriados.isEmpty() ? ResponseEntity.ok(feriados) : ResponseEntity.notFound().build();
    }


    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public void cadastro(@RequestBody @Valid CreateFeriado dadosFeriado) {
        service.cadastrarFeriado(dadosFeriado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        service.deleteById(id);
    }

}
