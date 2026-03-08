package com.marcweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import com.marcweb.entity.User;
import com.marcweb.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogAuditoriaServiceTest {

    @Mock
    private LogAuditoriaRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LogAuditoriaService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setNome("Test User");
    }

    @Test
    void registrarLog_DeveSalvarLogComSucesso() throws Exception {
        // Given
        String acao = "Test Action";
        CategoriaLog categoria = CategoriaLog.ADMIN;
        StatusLog status = StatusLog.SUCESSO;
        Object detalhes = "Test details";

        // When
        service.registrarLog(user, acao, categoria, status, detalhes);

        // Then
        verify(repository).save(any());
    }

    @Test
    void buscarLogsPorUsuario_DeveRetornarLogsDoUsuario() {
        // Given
        Long userId = 1L;

        // When
        var logs = service.buscarLogsPorUsuario(userId);

        // Then
        verify(repository).findByUserId(userId, PageRequest.of(0, 10));
    }

    @Test
    void buscarLogsPorPeriodo_DeveRetornarLogsNoPeriodo() {
        // Given
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now();

        // When
        service.buscarLogsPorPeriodo(inicio, fim);

        // Then
        verify(repository).findByTimestampBetween(inicio, fim);
    }

    @Test
    void buscarLogsPorCategoria_DeveRetornarLogsDaCategoria() {
        // Given
        CategoriaLog categoria = CategoriaLog.PONTO;

        // When
        service.buscarLogsPorCategoria(categoria);

        // Then
        verify(repository).findByCategoria(categoria);
    }

    @Test
    void buscarLogsPorStatus_DeveRetornarLogsDoStatus() {
        // Given
        StatusLog status = StatusLog.ERRO;

        // When
        service.buscarLogsPorStatus(status);

        // Then
        verify(repository).findByStatus(status);
    }
}
