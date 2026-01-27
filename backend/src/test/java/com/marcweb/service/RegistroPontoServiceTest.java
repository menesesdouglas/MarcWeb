package com.marcweb.service;

import com.marcweb.dto.CreateRegistroPonto;
import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.TipoRegistro;
import com.marcweb.entity.User;
import com.marcweb.exceptions.*;
import com.marcweb.repository.FeriadoRepository;
import com.marcweb.repository.RegistroPontoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroPontoServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private RegistroPontoRepository repository;

    @Mock
    private ConfigEmpresaService configEmpresaService;

    @Mock
    private FeriadoRepository feriadoRepository;

    @Mock
    private FolgaService folgaService;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @InjectMocks
    private RegistroPontoService registroPontoService;

    private User user;
    private CreateRegistroPonto createRegistroPonto;
    private ConfigEmpresa configEmpresa;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setNome("Test User");

        createRegistroPonto = new CreateRegistroPonto();
        createRegistroPonto.setTipo(TipoRegistro.ENTRADA);
        createRegistroPonto.setLatitude(-23.5505);
        createRegistroPonto.setLongitude(-46.6333);

        configEmpresa = new ConfigEmpresa();
        configEmpresa.setGeolocAtiva(true);
        configEmpresa.setLatCentro(-23.5505);
        configEmpresa.setLongCentro(-46.6333);
        configEmpresa.setRaioGeoloc(1000);

        request = mock(HttpServletRequest.class);
    }

    @Test
    void registrarPonto_DeveLancarExcecao_QuandoUsuarioEmFolga() {
        // Arrange
        when(folgaService.existsByUserAndDate(anyLong(), any(LocalDate.class))).thenReturn(true);

        // Act & Assert
        assertThrows(RegistroEmFolgaException.class, () ->
            registroPontoService.registrarPonto(createRegistroPonto, request)
        );
    }

    @Test
    void registrarPonto_DeveLancarExcecao_QuandoDomingo() {
        // Arrange
        when(folgaService.existsByUserAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
        when(UserService.getUserDetailsBySecurityContext()).thenReturn(mock(UserDetails.class));
        when(userService.findByUsername(anyString())).thenReturn(user);

        // Simular domingo
        LocalDate domingo = LocalDate.of(2023, 1, 1); // 01/01/2023 foi um domingo
        when(LocalDate.now()).thenReturn(domingo);

        // Act & Assert
        assertThrows(RegistroEmDomingoException.class, () ->
            registroPontoService.registrarPonto(createRegistroPonto, request)
        );
    }

    @Test
    void registrarPonto_DeveLancarExcecao_QuandoFeriado() {
        // Arrange
        when(folgaService.existsByUserAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
        when(UserService.getUserDetailsBySecurityContext()).thenReturn(mock(UserDetails.class));
        when(userService.findByUsername(anyString())).thenReturn(user);
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(Collections.singletonList(new com.marcweb.entity.Feriado()));

        // Act & Assert
        assertThrows(RegistroEmFeriadoException.class, () ->
            registroPontoService.registrarPonto(createRegistroPonto, request)
        );
    }

    @Test
    void registrarPonto_DeveLancarExcecao_QuandoGeolocalizacaoObrigatoriaENaoFornecida() {
        // Arrange
        when(folgaService.existsByUserAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
        when(UserService.getUserDetailsBySecurityContext()).thenReturn(mock(UserDetails.class));
        when(userService.findByUsername(anyString())).thenReturn(user);
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new com.marcweb.dto.DataConfigEmpresa(configEmpresa));

        createRegistroPonto.setLatitude(null);
        createRegistroPonto.setLongitude(null);

        // Act & Assert
        assertThrows(GeolocalizacaoObrigatoriaException.class, () ->
            registroPontoService.registrarPonto(createRegistroPonto, request)
        );
    }

    @Test
    void registrarPonto_DeveLancarExcecao_QuandoForaDoRaio() {
        // Arrange
        when(folgaService.existsByUserAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
        when(UserService.getUserDetailsBySecurityContext()).thenReturn(mock(UserDetails.class));
        when(userService.findByUsername(anyString())).thenReturn(user);
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new com.marcweb.dto.DataConfigEmpresa(configEmpresa));

        // Coordenadas fora do raio (10km de distância)
        createRegistroPonto.setLatitude(-23.6000);
        createRegistroPonto.setLongitude(-46.7000);

        // Act & Assert
        assertThrows(RegistroForaDoRaioException.class, () ->
            registroPontoService.registrarPonto(createRegistroPonto, request)
        );
    }

    @Test
    void registrarPonto_DeveLancarExcecao_QuandoSequenciaInvalida() {
        // Arrange
        when(folgaService.existsByUserAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
        when(UserService.getUserDetailsBySecurityContext()).thenReturn(mock(UserDetails.class));
        when(userService.findByUsername(anyString())).thenReturn(user);
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new com.marcweb.dto.DataConfigEmpresa(configEmpresa));
        when(repository.findLastTypeRegistration(anyLong())).thenReturn(TipoRegistro.SAIDA);

        // Tentar registrar ENTRADA depois de SAIDA é válido, mas INICIO_DESCANSO não é
        createRegistroPonto.setTipo(TipoRegistro.INICIO_DESCANSO);

        // Act & Assert
        assertThrows(RegistroPontoSequenciaInvalidaException.class, () ->
            registroPontoService.registrarPonto(createRegistroPonto, request)
        );
    }

    @Test
    void registrarPonto_DeveRegistrarComSucesso_QuandoTudoValido() throws Exception {
        // Arrange
        when(folgaService.existsByUserAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
        when(UserService.getUserDetailsBySecurityContext()).thenReturn(mock(UserDetails.class));
        when(userService.findByUsername(anyString())).thenReturn(user);
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new com.marcweb.dto.DataConfigEmpresa(configEmpresa));
        when(repository.findLastTypeRegistration(anyLong())).thenReturn(null);
        when(repository.save(any(RegistroPonto.class))).thenReturn(new RegistroPonto());
        when(request.getHeader(anyString())).thenReturn("192.168.1.1");

        // Act
        registroPontoService.registrarPonto(createRegistroPonto, request);

        // Assert
        verify(repository, times(1)).save(any(RegistroPonto.class));
    }

    @Test
    void buscarPontosRegistrados_DeveRetornarListaVazia_QuandoNaoHaRegistros() {
        // Arrange
        when(UserService.getUserDetailsBySecurityContext()).thenReturn(mock(UserDetails.class));
        when(userService.findByUsername(anyString())).thenReturn(user);
        when(repository.findByUserAndTimestampBetweenOrderByTimestampAsc(any(User.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        var resultado = registroPontoService.buscarPontosRegistrados(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        // Assert
        assertTrue(resultado.getRegistros().isEmpty());
    }

    @Test
    void findByMesAno_DeveRetornarListaVazia_QuandoNaoHaRegistros() {
        // Arrange
        when(repository.buscarRegistrosEmIntervaloDeData(any(User.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        var resultado = registroPontoService.findByMesAno(1L, 1, 2023);

        // Assert
        assertTrue(resultado.isEmpty());
    }
}
