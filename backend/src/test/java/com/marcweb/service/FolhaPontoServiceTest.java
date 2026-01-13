package com.marcweb.service;

import com.marcweb.dto.folha_ponto.DadosFolhaPonto;
import com.marcweb.entity.FolhaPonto;
import com.marcweb.entity.User;
import com.marcweb.exceptions.FolhaPontoDuplicadaException;
import com.marcweb.exceptions.SemRegistrosException;
import com.marcweb.repository.FolhaPontoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolhaPontoServiceTest {

    @Mock
    private FeriadoService feriadoService;

    @Mock
    private UserService userService;

    @Mock
    private RegistroPontoService registroPontoService;

    @Mock
    private FolgaService folgaService;

    @Mock
    private FolhaPontoRepository repository;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @InjectMocks
    private FolhaPontoService folhaPontoService;

    private User user;
    private FolhaPonto folhaPontoExistente;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setNome("Test User");
        user.setUsername("testuser");

        folhaPontoExistente = new FolhaPonto();
        folhaPontoExistente.setId(1L);
        folhaPontoExistente.setUser(user);
        folhaPontoExistente.setMes(1);
        folhaPontoExistente.setAno(2023);
        folhaPontoExistente.setDados("{\"test\": \"data\"}");
    }

    @Test
    void gerarFolhaPonto_DeveRetornarFolhaExistente_QuandoJaExistir() throws Exception {
        // Arrange
        when(userService.findById(anyLong())).thenReturn(user);
        when(repository.findByUserIdAndMesAndAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(folhaPontoExistente);

        // Act
        String resultado = folhaPontoService.gerarFolhaPonto(1L, 1, 2023);

        // Assert
        assertEquals("{\"test\": \"data\"}", resultado);
        verify(repository, never()).save(any());
    }

    @Test
    void gerarFolhaPonto_DeveLancarExcecao_QuandoNaoHaRegistros() {
        // Arrange
        when(userService.findById(anyLong())).thenReturn(user);
        when(repository.findByUserIdAndMesAndAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(null);
        when(registroPontoService.findByMesAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(java.util.Collections.emptyList());
        when(folgaService.findByPeriodo(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        assertThrows(SemRegistrosException.class, () ->
            folhaPontoService.gerarFolhaPonto(1L, 1, 2023)
        );
    }

    @Test
    void gerarFolhaPonto_DeveGerarNovaFolha_QuandoNaoExistir() throws Exception {
        // Arrange
        when(userService.findById(anyLong())).thenReturn(user);
        when(repository.findByUserIdAndMesAndAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(null);
        when(registroPontoService.findByMesAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(java.util.Collections.emptyList());
        when(folgaService.findByPeriodo(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(java.util.Collections.emptyList());
        when(repository.save(any(FolhaPonto.class))).thenAnswer(invocation -> {
            FolhaPonto folha = invocation.getArgument(0);
            folha.setId(1L);
            return folha;
        });

        // Act
        String resultado = folhaPontoService.gerarFolhaPonto(1L, 1, 2023);

        // Assert
        assertNotNull(resultado);
        verify(repository, times(1)).save(any(FolhaPonto.class));
    }

}
