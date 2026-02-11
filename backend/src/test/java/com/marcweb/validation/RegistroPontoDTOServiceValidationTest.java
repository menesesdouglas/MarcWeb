package com.marcweb.validation;

import com.marcweb.dto.ponto.CreateRegistroPonto;
import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.dto.config_empresa.DataConfigEmpresa;
import com.marcweb.entity.Role;
import com.marcweb.entity.TipoRegistro;
import com.marcweb.entity.User;
import com.marcweb.exceptions.*;
import com.marcweb.repository.FeriadoRepository;
import com.marcweb.repository.RegistroPontoRepository;
import com.marcweb.service.ConfigEmpresaService;
import com.marcweb.service.RegistroPontoService;
import com.marcweb.service.FolgaService;
import com.marcweb.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroPontoDTOServiceValidationTest {

    @Mock
    private UserService userService;

    @Mock
    private RegistroPontoRepository registroPontoRepository;

    @Mock
    private ConfigEmpresaService configEmpresaService;

    @Mock
    private FeriadoRepository feriadoRepository;

    @Mock
    private FolgaService folgaService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RegistroPontoService registroPontoService;

    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test.user");
        testUser.setRole(Role.EMPLOYEE);

        when(userService.findByUsername(anyString())).thenReturn(testUser);
        when(folgaService.existsByUserAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);

        // Configurar UserDetails mock
        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test.user");

        // Configurar mock padrão para ConfigEmpresaService (geolocalização desativada)
        ConfigEmpresa defaultConfig = ConfigEmpresa.builder()
                .geolocAtiva(false)
                .build();
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(defaultConfig));
    }


    @Test
    void validarRegistro_PrimeiroRegistroDeveSerEntrada() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any())).thenReturn(java.util.Optional.empty());
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
        }
    }

    @Test
    void validarRegistro_PrimeiroRegistroNaoEntradaDeveFalhar() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any())).thenReturn(java.util.Optional.empty());
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.INICIO_DESCANSO);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertThrows(RegistroPontoSequenciaInvalidaException.class,
                () -> registroPontoService.registrarPonto(request, this.request));
        }
    }

    @Test
    void validarRegistro_SequenciaInvalidaDeveFalhar() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any()))
            .thenReturn(java.util.Optional.of(TipoRegistro.ENTRADA));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.SAIDA);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertThrows(RegistroPontoSequenciaInvalidaException.class,
                () -> registroPontoService.registrarPonto(request, this.request));
        }
    }


    @Test
    void validarRegistro_SequenciaValidaEntradaParaInicioDescanso() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any()))
            .thenReturn(java.util.Optional.of(TipoRegistro.ENTRADA));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.INICIO_DESCANSO);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
        }
    }

    @Test
    void validarRegistro_SequenciaValidaInicioParaFimDescanso() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any()))
            .thenReturn(java.util.Optional.of(TipoRegistro.INICIO_DESCANSO));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.FIM_DESCANSO);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
        }
    }

    @Test
    void validarRegistro_SequenciaValidaFimDescansoParaSaida() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any()))
            .thenReturn(java.util.Optional.of(TipoRegistro.FIM_DESCANSO));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.SAIDA);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
        }
    }

    @Test
    void validarRegistro_SequenciaValidaSaidaParaEntrada() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any()))
            .thenReturn(java.util.Optional.of(TipoRegistro.SAIDA));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
        }
    }

    @Test
    void validarDiaUtil_DomingoDeveFalhar() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any())).thenReturn(java.util.Optional.empty());
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        // Simular domingo
        LocalDate domingo = LocalDate.of(2026, 1, 4); // 04/01/2026 é domingo

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class);
             MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            mockedLocalDate.when(LocalDate::now).thenReturn(domingo);

            CreateRegistroPonto createRequest = new CreateRegistroPonto();
            createRequest.setTipo(TipoRegistro.ENTRADA);

            assertThrows(RegistroEmDomingoException.class,
                () -> registroPontoService.registrarPonto(createRequest, this.request));
        }
    }



    @Test
    void validarDiaUtil_SabadoDevePermitir() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any())).thenReturn(java.util.Optional.empty());
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        // Simular sábado
        LocalDate sabado = LocalDate.of(2026, 1, 3); // 03/01/2026 é sábado

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class);
             MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            mockedLocalDate.when(LocalDate::now).thenReturn(sabado);

            CreateRegistroPonto request = new CreateRegistroPonto();
            request.setTipo(TipoRegistro.ENTRADA);

            assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
        }
    }


    @Test
    void validarGeolocalizacao_ForaDoRaioDeveFalhar() {
        ConfigEmpresa config = ConfigEmpresa.builder()
            .geolocAtiva(true)
            .latCentro(-12.2658)
            .longCentro(-38.9668)
            .raioGeoloc(50)
            .build();

        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any())).thenReturn(java.util.Optional.empty());
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        // Coordenadas fora do raio (200m de distância)
        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        request.setLatitude(-12.2678);
        request.setLongitude(-38.9685);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertThrows(RegistroForaDoRaioException.class,
                () -> registroPontoService.registrarPonto(request, this.request));
        }
    }


    @Test
    void validarGeolocalizacao_DentroDoRaioDevePermitir() {
        ConfigEmpresa config = ConfigEmpresa.builder()
            .geolocAtiva(true)
            .latCentro(-12.2658)
            .longCentro(-38.9668)
            .raioGeoloc(100)
            .build();

        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any())).thenReturn(java.util.Optional.empty());
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        // Coordenadas dentro do raio (30m de distância)
        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        request.setLatitude(-12.2661);
        request.setLongitude(-38.9670);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
        }
    }

    @Test
    void validarGeolocalizacao_GeolocDesativadaDeveIgnorar() {
        ConfigEmpresa config = ConfigEmpresa.builder()
            .geolocAtiva(false)
            .latCentro(-12.2658)
            .longCentro(-38.9668)
            .raioGeoloc(50)
            .build();

        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any())).thenReturn(java.util.Optional.empty());
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        // Coordenadas fora do raio, mas geoloc desativada
        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        request.setLatitude(-12.2678);
        request.setLongitude(-38.9685);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
        }
    }


    @Test
    void validarGeolocalizacao_SemCoordenadasComGeolocAtivaDeveFalhar() {
        ConfigEmpresa config = ConfigEmpresa.builder()
            .geolocAtiva(true)
            .latCentro(-12.2658)
            .longCentro(-38.9668)
            .raioGeoloc(50)
            .build();

        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any())).thenReturn(java.util.Optional.empty());
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        // Sem latitude e longitude

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertThrows(GeolocalizacaoObrigatoriaException.class,
                () -> registroPontoService.registrarPonto(request, this.request));
        }
    }


    @Test
    void validarGeolocalizacao_SemConfiguracaoDeCoordenadasDeveFalhar() {
        ConfigEmpresa config = ConfigEmpresa.builder()
            .geolocAtiva(true)
            .latCentro(null) // Sem coordenadas configuradas
            .longCentro(null)
            .raioGeoloc(50)
            .build();

        when(registroPontoRepository.findLastTypeRegistration(anyLong(), any(), any())).thenReturn(java.util.Optional.empty());
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));
        when(feriadoRepository.findByData(any(LocalDate.class))).thenReturn(List.of());

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        request.setLatitude(-12.2661);
        request.setLongitude(-38.9670);

        try (MockedStatic<UserService> mockedUserService = Mockito.mockStatic(UserService.class)) {
            mockedUserService.when(UserService::getUserDetailsBySecurityContext).thenReturn(userDetails);
            assertThrows(IllegalStateException.class,
                () -> registroPontoService.registrarPonto(request, this.request));
        }
    }

}
