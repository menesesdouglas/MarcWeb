package com.marcweb.dto;

import com.marcweb.dto.user.UserResponse;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.LogAuditoria;
import com.marcweb.entity.StatusLog;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;


@Data
public class LogAuditoriaResponse {
    private Long id;
    private LocalDateTime timestamp; // Data e hora do log
    private UserResponse user; // Usuário que realizou a ação
    private String acao; // Descrição da ação (ex: login, registro_ponto)
    private CategoriaLog categoria; // PONTO, ADMIN, AUDITORIA, etc.
    private String detalhes;
    private StatusLog status; // SUCESSO, ERRO

    public LogAuditoriaResponse(LogAuditoria logAuditoria) {
        BeanUtils.copyProperties(logAuditoria, this);
        this.user = new UserResponse(logAuditoria.getUser());
    }
}
