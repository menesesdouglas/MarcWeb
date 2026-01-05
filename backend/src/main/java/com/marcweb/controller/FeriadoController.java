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
import java.util.Map; // Import necessário

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/feriado") // Mantém o prefixo base
public class FeriadoController {

    private final BrasilApiService brasilApiService;
    private final FeriadoService service;

    // Endpoint antigo (mantido, opcionalmente pode ser depreciado)
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/nacionais") // Antigo endpoint
    public void cadastrarFeriadosViaAPI() {
        // Chama o método antigo que agora internamente usa o novo com ano atual
        brasilApiService.cadastrarFeriadosNacionais(); // Este método agora chama sincronizarFeriados(Year.now().getValue())
    }

    // NOVO Endpoint conforme SDD e BUG-009
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync") // Novo endpoint mais descritivo
    public ResponseEntity<Map<String, Integer>> sincronizarFeriados(@RequestParam int ano) { // Recebe ano como parâmetro
        // Chama o método do service que retorna as estatísticas
        Map<String, Integer> estatisticas = brasilApiService.sincronizarFeriados(ano);
        // Retorna as estatísticas com status 200 OK
        return ResponseEntity.ok(estatisticas);
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