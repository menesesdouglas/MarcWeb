package com.marcweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.dto.LogAuditoriaResponse;
import com.marcweb.entity.LogAuditoria;
import com.marcweb.entity.User;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.repository.LogAuditoriaRepository;
import com.marcweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LogAuditoriaService {

    private final LogAuditoriaRepository repository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;



    @Transactional(propagation = Propagation.REQUIRES_NEW) /// PARA NÃO DAR ROLLBACK NO AUDITADO DE ERRO
    public void registrarLog(User user, String acao, CategoriaLog categoria, StatusLog status, Object detalhes) throws JsonProcessingException {
        String detalhesJson = detalhes != null ? objectMapper.writeValueAsString(detalhes) : null;
        LogAuditoria log = new LogAuditoria(user, acao, categoria, detalhesJson, status);
        repository.save(log);
    }

    public List<LogAuditoriaResponse> buscarLogsPorUsuario(Long userId) {
        return repository.findByUserId(userId, PageRequest.of(0, 10)).toList();
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

    public Page<LogAuditoriaResponse> buscarLogsComFiltros(Long userId, LocalDateTime inicio, LocalDateTime fim,
                                                           CategoriaLog categoria, StatusLog status, String acao,
                                                           Pageable pageable) {
        return repository.findWithFilters(userId, inicio, fim, categoria, status, acao, pageable);
    }

    // NOVO Método: Buscar logs do usuário autenticado
    public List<LogAuditoriaResponse> buscarLogsPorMe() {
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        User user = userRepository.findByUsernameIgnoreCase(userDetails.getUsername()).orElseThrow(
                () -> new RuntimeException("Usuário não encontrado")
        );
        return repository.findByUserId(user.getId(), PageRequest.of(0, 10)).toList();
    }
}
