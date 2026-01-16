package com.marcweb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.marcweb.auditoria.Auditado;
import com.marcweb.dto.folha_ponto.DadosFolhaPonto;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.service.FolhaPontoService;
import com.marcweb.service.LogAuditoriaService;
import com.marcweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/folha-ponto")
public class FolhaPontoController {

    private final FolhaPontoService service;
    private final UserService userService;


    @Auditado(
            acao = "Gerando folha de ponto para de mês e ano específico",
            categoria = CategoriaLog.ADMIN,
            detalhes = "Administrador executou a geração de folha de ponto para usuário"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/gerar")
    public String gerarFolhaPonto(@RequestParam Long userId, @RequestParam int mes, @RequestParam int ano) throws JsonProcessingException {
        var dados = service.gerarFolhaPonto(userId, mes, ano);
        return dados;
    }

    @Auditado(
            acao = "Gerando folha de ponto para de mês e ano específico",
            categoria = CategoriaLog.ADMIN,
            detalhes = "Usuário gerou propria folha de ponto"
    )
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/me")
    public String getMyFolhaPonto(@RequestParam int mes, @RequestParam int ano, Authentication authentication) throws JsonProcessingException {
        String username = authentication.getName();
        var user = userService.findByUsername(username);
        return service.gerarFolhaPonto(user.getId(), mes, ano);
    }
}
