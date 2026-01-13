package com.marcweb.auditoria;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.marcweb.entity.LogAuditoria;
import com.marcweb.entity.StatusLog;
import com.marcweb.entity.User;
import com.marcweb.service.LogAuditoriaService;
import com.marcweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Aspect
@Component
public class AuditoriaAspec {

    private final UserService userService;
    private final LogAuditoriaService service;


    /// ♦♦♦♦♦♦♦♦♦♦♦   ♦♦♦♦♦
    @AfterReturning("@annotation(auditado)")
    public void auditarSucesso(Auditado auditado) throws JsonProcessingException {
        User usuarioLogado = userService.findByUsername(
                UserService.getUserDetailsBySecurityContext().getUsername()
        );
        service.registrarLog(usuarioLogado, auditado.acao(), auditado.categoria(), StatusLog.SUCESSO, auditado.detalhes());
    }


    /// ♥♥♥♥♥♥♥♥♥♥♥
    @AfterThrowing(pointcut = "@annotation(auditado)",
            throwing = "e")
    public void auditarErro(Auditado auditado, Exception e) throws JsonProcessingException {
        User usuarioLogado = userService.findByUsername(
                UserService.getUserDetailsBySecurityContext().getUsername()
        );
        service.registrarLog(usuarioLogado, auditado.acao(), auditado.categoria(), StatusLog.ERRO, e.getMessage());
    }
}
