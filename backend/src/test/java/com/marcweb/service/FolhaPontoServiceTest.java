package com.marcweb.service;

import com.marcweb.dto.folha_ponto.DadosFolhaPonto;
import com.marcweb.entity.*;
import com.marcweb.exceptions.FolhaPontoDuplicadaException;
import com.marcweb.exceptions.SemRegistrosException;
import com.marcweb.repository.FolhaPontoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
        List<RegistroPonto> registros = Arrays.asList(
            createRegistroPonto(TipoRegistro.ENTRADA, LocalDateTime.of(2023, 1, 2, 8, 0)),
            createRegistroPonto(TipoRegistro.SAIDA, LocalDateTime.of(2023, 1, 2, 17, 0))
        );

        when(userService.findById(anyLong())).thenReturn(user);
        when(repository.findByUserIdAndMesAndAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(null);
        when(registroPontoService.findByMesAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(registros);
        when(folgaService.findByPeriodo(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(feriadoService.findByDataBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
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

    @Test
    void gerarFolhaPonto_DeveCalcularHorasCorretamente_QuandoHaRegistrosCompletos() throws Exception {
        // Arrange
        List<RegistroPonto> registros = Arrays.asList(
            createRegistroPonto(TipoRegistro.ENTRADA, LocalDateTime.of(2023, 1, 2, 8, 0)),
            createRegistroPonto(TipoRegistro.INICIO_DESCANSO, LocalDateTime.of(2023, 1, 2, 12, 0)),
            createRegistroPonto(TipoRegistro.FIM_DESCANSO, LocalDateTime.of(2023, 1, 2, 13, 0)),
            createRegistroPonto(TipoRegistro.SAIDA, LocalDateTime.of(2023, 1, 2, 17, 0))
        );

        when(userService.findById(anyLong())).thenReturn(user);
        when(repository.findByUserIdAndMesAndAno(anyLong(), anyInt(), anyInt())).thenReturn(null);
        when(registroPontoService.findByMesAno(anyLong(), anyInt(), anyInt())).thenReturn(registros);
        when(folgaService.findByPeriodo(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(feriadoService.findByDataBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(repository.save(any(FolhaPonto.class))).thenAnswer(invocation -> {
            FolhaPonto folha = invocation.getArgument(0);
            folha.setId(1L);
            return folha;
        });

        // Act
        String resultado = folhaPontoService.gerarFolhaPonto(1L, 1, 2023);

        // Assert
        assertNotNull(resultado);
        // 8h trabalhadas (de 8:00 a 12:00 = 4h, 13:00 a 17:00 = 4h)
        assertTrue(resultado.contains("\"horas_trabalhadas\":8.00"));
    }

    @Test
    void gerarFolhaPonto_DeveCalcularHorasExtras_QuandoUltrapassar8hDiarias() throws Exception {
        // Arrange
        List<RegistroPonto> registros = Arrays.asList(
            createRegistroPonto(TipoRegistro.ENTRADA, LocalDateTime.of(2023, 1, 2, 8, 0)),
            createRegistroPonto(TipoRegistro.SAIDA, LocalDateTime.of(2023, 1, 2, 18, 0))
        );

        when(userService.findById(anyLong())).thenReturn(user);
        when(repository.findByUserIdAndMesAndAno(anyLong(), anyInt(), anyInt())).thenReturn(null);
        when(registroPontoService.findByMesAno(anyLong(), anyInt(), anyInt())).thenReturn(registros);
        when(folgaService.findByPeriodo(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(feriadoService.findByDataBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(repository.save(any(FolhaPonto.class))).thenAnswer(invocation -> {
            FolhaPonto folha = invocation.getArgument(0);
            folha.setId(1L);
            return folha;
        });

        // Act
        String resultado = folhaPontoService.gerarFolhaPonto(1L, 1, 2023);

        // Assert
        assertNotNull(resultado);
        // 10h trabalhadas, 2h extras
        assertTrue(resultado.contains("\"horas_trabalhadas\":10.00"));
        assertTrue(resultado.contains("\"horas_extras\":2"));
    }

    @Test
    void gerarFolhaPonto_DeveCalcularHorasDebito_QuandoMenosDe8h() throws Exception {
        // Arrange
        List<RegistroPonto> registros = Arrays.asList(
            createRegistroPonto(TipoRegistro.ENTRADA, LocalDateTime.of(2023, 1, 2, 8, 0)),
            createRegistroPonto(TipoRegistro.SAIDA, LocalDateTime.of(2023, 1, 2, 15, 0))
        );

        when(userService.findById(anyLong())).thenReturn(user);
        when(repository.findByUserIdAndMesAndAno(anyLong(), anyInt(), anyInt())).thenReturn(null);
        when(registroPontoService.findByMesAno(anyLong(), anyInt(), anyInt())).thenReturn(registros);
        when(folgaService.findByPeriodo(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(feriadoService.findByDataBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(repository.save(any(FolhaPonto.class))).thenAnswer(invocation -> {
            FolhaPonto folha = invocation.getArgument(0);
            folha.setId(1L);
            return folha;
        });

        // Act
        String resultado = folhaPontoService.gerarFolhaPonto(1L, 1, 2023);

        // Assert
        assertNotNull(resultado);
        // 7h trabalhadas, 1h débito
        assertTrue(resultado.contains("\"horas_trabalhadas\":7.00"));
        assertTrue(resultado.contains("\"horas_debito\":1"));
    }

    private RegistroPonto createRegistroPonto(TipoRegistro tipo, LocalDateTime timestamp) {
        RegistroPonto registro = new RegistroPonto();
        registro.setTipo(tipo);
        registro.setTimestamp(timestamp);
        registro.setUser(user);
        return registro;
    }

}
