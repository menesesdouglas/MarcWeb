package com.marcweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.marcweb.entity.User;
import com.marcweb.exceptions.AEJException;
import com.marcweb.exceptions.ConformidadeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConformidadeServiceTest {

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @Mock
    private FolhaPontoService folhaPontoService;

    @Mock
    private AEJService aejService;

    @Mock
    private AssinaturaDigitalService assinaturaDigitalService;

    @InjectMocks
    private ConformidadeService conformidadeService;

    private User user;
    private String folhaPontoJson;
    private String chavePrivada;
    private String chavePublica;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setNome("Test User");
        user.setUsername("testuser");

        folhaPontoJson = "{\"usuario\": {\"id\": 1, \"nome\": \"Test User\"}, \"periodo\": {\"mes\": 1, \"ano\": 2023}}";
        chavePrivada = "chave-privada-base64";
        chavePublica = "chave-publica-base64";
    }

    @Test
    void validarConformidadeFolhaPonto_DeveRetornarSucesso_QuandoValido() throws Exception {
        // Arrange
        when(folhaPontoService.gerarFolhaPonto(anyLong(), anyInt(), anyInt()))
                .thenReturn(folhaPontoJson);

        // Act
        Map<String, Object> resultado = conformidadeService.validarConformidadeFolhaPonto(1L, 1, 2023);

        // Assert
        assertNotNull(resultado);
        assertEquals("SUCCESS", resultado.get("status"));
        assertTrue((Boolean) resultado.get("conforme"));
    }

    @Test
    void validarConformidadeFolhaPonto_DeveLancarExcecao_QuandoInvalido() throws Exception {
        // Arrange
        when(folhaPontoService.gerarFolhaPonto(anyLong(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Erro ao gerar folha"));

        // Act & Assert
        assertThrows(ConformidadeException.class, () ->
            conformidadeService.validarConformidadeFolhaPonto(1L, 1, 2023)
        );
    }

    @Test
    void gerarEASSinar_DeveRetornarAEJAssinado_QuandoValido() throws Exception {
        // Arrange
        when(folhaPontoService.gerarFolhaPonto(anyLong(), anyInt(), anyInt()))
                .thenReturn(folhaPontoJson);
        when(aejService.gerarAEJ(anyString(), any(User.class), anyString()))
                .thenReturn("{\"aej\": \"dados\", \"assinatura\": \"assinatura\"}");

        // Act
        String resultado = conformidadeService.gerarEASSinar(1L, 1, 2023, user, chavePrivada);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("aej"));
        assertTrue(resultado.contains("assinatura"));
    }

    @Test
    void gerarEASSinar_DeveLancarExcecao_QuandoErroAoGerarAEJ() throws Exception {
        // Arrange
        when(folhaPontoService.gerarFolhaPonto(anyLong(), anyInt(), anyInt()))
                .thenReturn(folhaPontoJson);
        when(aejService.gerarAEJ(anyString(), any(User.class), anyString()))
                .thenThrow(new RuntimeException("Erro ao gerar AEJ"));

        // Act & Assert
        assertThrows(ConformidadeException.class, () ->
            conformidadeService.gerarEASSinar(1L, 1, 2023, user, chavePrivada)
        );
    }

    @Test
    void validarAssinaturaAEJ_DeveRetornarTrue_QuandoAssinaturaValida() throws JsonProcessingException, AEJException, ConformidadeException {
        // Arrange
        String aejJson = "{\"aej\": \"dados\", \"assinatura\": \"assinatura-valida\"}";
        when(aejService.validarAEJ(anyString(), anyString())).thenReturn(true);

        // Act
        boolean resultado = conformidadeService.validarAssinaturaAEJ(aejJson, chavePublica);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void validarAssinaturaAEJ_DeveRetornarFalse_QuandoAssinaturaInvalida() throws JsonProcessingException, AEJException, ConformidadeException {
        // Arrange
        String aejJson = "{\"aej\": \"dados\", \"assinatura\": \"assinatura-invalida\"}";
        when(aejService.validarAEJ(anyString(), anyString())).thenReturn(false);

        // Act
        boolean resultado = conformidadeService.validarAssinaturaAEJ(aejJson, chavePublica);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarAssinaturaAEJ_DeveLancarExcecao_QuandoErroAoValidar() throws JsonProcessingException, AEJException, ConformidadeException {
        // Arrange
        String aejJson = "{\"aej\": \"dados\", \"assinatura\": \"assinatura-invalida\"}";
        when(aejService.validarAEJ(anyString(), anyString()))
                .thenThrow(new RuntimeException("Erro ao validar"));

        // Act & Assert
        assertThrows(ConformidadeException.class, () ->
            conformidadeService.validarAssinaturaAEJ(aejJson, chavePublica)
        );
    }

    @Test
    void validarConformidadeComPortaria671_DeveRetornarRelatorio_QuandoValidado() throws Exception {
        // Arrange
        when(folhaPontoService.gerarFolhaPonto(anyLong(), anyInt(), anyInt()))
                .thenReturn(folhaPontoJson);

        // Act
        Map<String, Object> resultado = conformidadeService.validarConformidadeComPortaria671(1L, 1, 2023);

        // Assert
        assertNotNull(resultado);
        assertEquals("SUCCESS", resultado.get("status"));
        assertTrue((Boolean) resultado.get("conformePortaria671"));
    }
}
