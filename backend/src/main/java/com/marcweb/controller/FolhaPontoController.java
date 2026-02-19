package com.marcweb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.marcweb.auditoria.Auditado;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.service.FolhaPontoService;
import com.marcweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/folha-ponto")
public class FolhaPontoController {

    private final FolhaPontoService service;
    private final UserService userService;


    @Auditado(
            acao = "Gerando folha de ponto para de mês e ano específico",
            categoria = CategoriaLog.ADMIN
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping()
    public String gerarFolhaPonto(@RequestParam Long userId, @RequestParam int mes, @RequestParam int ano) throws JsonProcessingException {
        var dados = service.gerarFolhaPonto(userId, mes, ano);
        return dados;
    }


    @Auditado(
            acao = "Buscando folha de ponto para de mês e ano específico",
            categoria = CategoriaLog.ADMIN
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String buscarFolhaPonto(@RequestParam Long userId, @RequestParam int mes, @RequestParam int ano) {
        return service.buscarFolhaPonto(userId, mes, ano);
    }

    @Auditado(
            acao = "Gerando folha de ponto para de mês e ano específico",
            categoria = CategoriaLog.AUDITORIA)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/me")
    public String postMyFolhaPonto(@RequestParam int mes, @RequestParam int ano, Authentication authentication) throws JsonProcessingException {
        String username = authentication.getName();
        var user = userService.findByUsername(username);
        return service.gerarFolhaPonto(user.getId(), mes, ano);
    }

    @Auditado(
            acao = "Gerando folha de ponto para de mês e ano específico",
            categoria = CategoriaLog.AUDITORIA)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/me")
    public String getMyFolhaPonto(@RequestParam int mes, @RequestParam int ano, Authentication authentication) throws JsonProcessingException {
        String username = authentication.getName();
        var user = userService.findByUsername(username);
        return service.buscarFolhaPonto(user.getId(), mes, ano);
    }
}
