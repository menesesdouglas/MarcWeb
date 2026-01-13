package com.marcweb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.marcweb.dto.folha_ponto.DadosFolhaPonto;
import com.marcweb.service.FolhaPontoService;
import com.marcweb.service.LogAuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/folha")
public class FolhaPontoController {

    private final FolhaPontoService service;
    private final LogAuditoriaService logAuditoriaService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String gerarFolhaPonto(@RequestParam Long userId, @RequestParam int mes, @RequestParam int ano) throws JsonProcessingException {
       var dados =  service.gerarFolhaPonto(userId, mes, ano);
      return dados;
    }
}
