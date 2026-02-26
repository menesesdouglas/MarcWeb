package com.marcweb.auditoria;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.marcweb.dto.login.LoginRequest;
import com.marcweb.entity.StatusLog;
import com.marcweb.entity.User;
import com.marcweb.service.LogAuditoriaService;
import com.marcweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
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
    private final Detalhes detalhes;


    /// AÇÕES AO TERMINAR UMA FUNÇÃO ANOTADA COM '@Auditato''
    @AfterReturning(pointcut = "@annotation(auditado)", returning = "retorno")
    public void auditarSucesso(JoinPoint jp, Auditado auditado, Object retorno) throws Throwable {
        String username = "";
        try {
            username = UserService.getUserDetailsBySecurityContext().getUsername();
        } catch (Exception e) {
            for (Object arg : jp.getArgs()) {
                if (arg instanceof LoginRequest) {
                    username = ((LoginRequest) arg).getUsername();
                }
            }
        }
        User usuario = userService.findByUsername(
                username
        );
        service.registrarLog(usuario, auditado.acao(), auditado.categoria(), StatusLog.SUCESSO, detalhes.getDetalhes());
        detalhes.setDetalhes("");
    }


    ///  AÇÕES AO OCORRER ERRO DURANTE A EXECUÇÃO DE UMA FUNÇÃO ANOTADA COM '@Auditato''
    @AfterThrowing(pointcut = "@annotation(auditado)",
            throwing = "e")
    public void auditarErro(Auditado auditado, Exception e) throws JsonProcessingException {
        User usuarioLogado = null;
        try {
            usuarioLogado = userService.findByUsername(
                    UserService.getUserDetailsBySecurityContext().getUsername()
            );
        } catch (Exception ex) {
            System.out.println("No pasa nada!!");
        }
        service.registrarLog(usuarioLogado, auditado.acao(), auditado.categoria(), StatusLog.ERRO, e.getMessage());
    }
}
