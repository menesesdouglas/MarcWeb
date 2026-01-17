package com.marcweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.dto.LogAuditoriaResponse;
import com.marcweb.entity.LogAuditoria;
import com.marcweb.entity.User;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LogAuditoriaService {

    private final LogAuditoriaRepository repository;
    private final ObjectMapper objectMapper;



    @Transactional(propagation = Propagation.REQUIRES_NEW) /// PARA NÃO DAR ROLLBACK NO AUDITADO DE ERRO
    public void registrarLog(User user, String acao, CategoriaLog categoria, StatusLog status, Object detalhes) throws JsonProcessingException {
        String detalhesJson = detalhes != null ? objectMapper.writeValueAsString(detalhes) : null;
        LogAuditoria log = new LogAuditoria(user, acao, categoria, detalhesJson, status);
        repository.save(log);
    }

    public List<LogAuditoriaResponse> buscarLogsPorUsuario(Long userId) {
        return repository.findByUserId(userId);
    }

    public List<LogAuditoriaResponse> buscarLogsPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByTimestampBetween(inicio, fim);
    }

    public List<LogAuditoriaResponse> buscarLogsPorCategoria(CategoriaLog categoria) {
        return repository.findByCategoria(categoria);
    }

    public List<LogAuditoriaResponse> buscarLogsPorStatus(StatusLog status) {
        return repository.findByStatus(status);
    }
}
