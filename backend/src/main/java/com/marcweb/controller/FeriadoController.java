package com.marcweb.controller;

import com.marcweb.dto.feriado.CreateFeriado;
import com.marcweb.entity.Feriado;
import com.marcweb.service.BrasilApiService;
import com.marcweb.service.FeriadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/feriado")
public class FeriadoController {

    private final BrasilApiService brasilApiService;
    private final FeriadoService service;


    // Conforme SDD e BUG-009
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Integer>> sincronizarFeriados(@RequestParam int ano) {
        Map<String, Integer> estatisticas = brasilApiService.sincronizarFeriados(ano);
        return ResponseEntity.ok(estatisticas);
    }

    @GetMapping
    public ResponseEntity<List<Feriado>> buscarFeriadosPorAno(
            @RequestParam("ano") int ano) {
        var feriados = service.findByAno(ano);
        return ResponseEntity.ok(feriados);
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


    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/atualizar")
    public void atualizarFeriadosAnoVigente() {
        service.atualizarFeriados();
    }

}