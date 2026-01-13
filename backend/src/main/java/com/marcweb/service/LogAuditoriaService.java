package com.marcweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.entity.LogAuditoria;
import com.marcweb.entity.User;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LogAuditoriaService {

    private final LogAuditoriaRepository repository;
    private final ObjectMapper objectMapper;

    public void registrarLog(User user, String acao, CategoriaLog categoria, StatusLog status, Object detalhes) throws JsonProcessingException {
        String detalhesJson = detalhes != null ? objectMapper.writeValueAsString(detalhes) : null;

        LogAuditoria log = LogAuditoria.builder()
                .timestamp(LocalDateTime.now())
                .user(user)
                .acao(acao)
                .categoria(categoria)
                .detalhes(detalhesJson)
                .status(status)
                .build();

        repository.save(log);
    }

    public List<LogAuditoria> buscarLogsPorUsuario(Long userId) {
        return repository.findByUserId(userId);
    }

    public List<LogAuditoria> buscarLogsPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByTimestampBetween(inicio, fim);
    }

    public List<LogAuditoria> buscarLogsPorCategoria(CategoriaLog categoria) {
        return repository.findByCategoria(categoria);
    }

    public List<LogAuditoria> buscarLogsPorStatus(StatusLog status) {
        return repository.findByStatus(status);
    }
}
