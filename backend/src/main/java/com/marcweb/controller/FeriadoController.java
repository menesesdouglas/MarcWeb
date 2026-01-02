package com.marcweb.controller;

import com.marcweb.dto.FeriadoDTO;
import com.marcweb.service.BrasilApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/feriado")
public class FeriadoController {

    private final BrasilApiService brasilApiService;


    // END POINT DE TEST DO 'BRASILAPISERVICE' FUNCIONANDO!!!
    @GetMapping
    public ResponseEntity<List<FeriadoDTO>> buscarFeriados() {
        var feriados = brasilApiService.buscarFeriados(2026);
        return ResponseEntity.ok(feriados);
    }
}
