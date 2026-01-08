package com.marcweb.validation;

import com.marcweb.dto.CreateRegistroPonto;
import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.dto.DataConfigEmpresa;
import com.marcweb.entity.Role;
import com.marcweb.entity.TipoRegistro;
import com.marcweb.entity.User;
import com.marcweb.exceptions.*;
import com.marcweb.repository.FeriadoRepository;
import com.marcweb.repository.RegistroPontoRepository;
import com.marcweb.service.ConfigEmpresaService;
import com.marcweb.service.RegistroPontoService;
import com.marcweb.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroPontoServiceValidationTest {

    @Mock
    private UserService userService;

    @Mock
    private RegistroPontoRepository registroPontoRepository;

    @Mock
    private ConfigEmpresaService configEmpresaService;

    @Mock
    private FeriadoRepository feriadoRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RegistroPontoService registroPontoService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test.user");
        testUser.setRole(Role.EMPLOYEE);

        when(userService.findByUsername(anyString())).thenReturn(testUser);
    }

    @Test
    void validarRegistro_PrimeiroRegistroDeveSerEntrada() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong())).thenReturn(null);

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);

        assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarRegistro_PrimeiroRegistroNaoEntradaDeveFalhar() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong())).thenReturn(null);

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.INICIO_DESCANSO);

        assertThrows(RegistroPontoSequenciaInvalidaException.class,
            () -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarRegistro_SequenciaInvalidaDeveFalhar() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong()))
            .thenReturn(TipoRegistro.ENTRADA);

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.SAIDA);

        assertThrows(RegistroPontoSequenciaInvalidaException.class,
            () -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarRegistro_SequenciaValidaEntradaParaInicioDescanso() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong()))
            .thenReturn(TipoRegistro.ENTRADA);

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.INICIO_DESCANSO);

        assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarRegistro_SequenciaValidaInicioParaFimDescanso() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong()))
            .thenReturn(TipoRegistro.INICIO_DESCANSO);

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.FIM_DESCANSO);

        assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarRegistro_SequenciaValidaFimDescansoParaSaida() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong()))
            .thenReturn(TipoRegistro.FIM_DESCANSO);

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.SAIDA);

        assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarRegistro_SequenciaValidaSaidaParaEntrada() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong()))
            .thenReturn(TipoRegistro.SAIDA);

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);

        assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarDiaUtil_DomingoDeveFalhar() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong())).thenReturn(null);

        // Simular domingo
        LocalDateTime domingo = LocalDateTime.of(2026, 1, 4, 10, 0); // 04/01/2026 é domingo

        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(domingo);

            CreateRegistroPonto request = new CreateRegistroPonto();
            request.setTipo(TipoRegistro.ENTRADA);

            assertThrows(RegistroEmDomingoException.class,
                () -> registroPontoService.registrarPonto(request, this.request));
        }
    }

    @Test
    void validarDiaUtil_SabadoDevePermitir() {
        when(registroPontoRepository.findLastTypeRegistration(anyLong())).thenReturn(null);

        // Simular sábado
        LocalDateTime sabado = LocalDateTime.of(2026, 1, 3, 10, 0); // 03/01/2026 é sábado

        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(sabado);

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

        when(registroPontoRepository.findLastTypeRegistration(anyLong())).thenReturn(null);
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));

        // Coordenadas fora do raio (200m de distância)
        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        request.setLatitude(-12.2678);
        request.setLongitude(-38.9685);

        assertThrows(RegistroForaDoRaioException.class,
            () -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarGeolocalizacao_DentroDoRaioDevePermitir() {
        ConfigEmpresa config = ConfigEmpresa.builder()
            .geolocAtiva(true)
            .latCentro(-12.2658)
            .longCentro(-38.9668)
            .raioGeoloc(100)
            .build();

        when(registroPontoRepository.findLastTypeRegistration(anyLong())).thenReturn(null);
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));

        // Coordenadas dentro do raio (30m de distância)
        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        request.setLatitude(-12.2661);
        request.setLongitude(-38.9670);

        assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarGeolocalizacao_GeolocDesativadaDeveIgnorar() {
        ConfigEmpresa config = ConfigEmpresa.builder()
            .geolocAtiva(false)
            .latCentro(-12.2658)
            .longCentro(-38.9668)
            .raioGeoloc(50)
            .build();

        when(registroPontoRepository.findLastTypeRegistration(anyLong())).thenReturn(null);
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));

        // Coordenadas fora do raio, mas geoloc desativada
        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        request.setLatitude(-12.2678);
        request.setLongitude(-38.9685);

        assertDoesNotThrow(() -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarGeolocalizacao_SemCoordenadasComGeolocAtivaDeveFalhar() {
        ConfigEmpresa config = ConfigEmpresa.builder()
            .geolocAtiva(true)
            .latCentro(-12.2658)
            .longCentro(-38.9668)
            .raioGeoloc(50)
            .build();

        when(registroPontoRepository.findLastTypeRegistration(anyLong())).thenReturn(null);
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        // Sem latitude e longitude

        assertThrows(GeolocalizacaoObrigatoriaException.class,
            () -> registroPontoService.registrarPonto(request, this.request));
    }

    @Test
    void validarGeolocalizacao_SemConfiguracaoDeCoordenadasDeveFalhar() {
        ConfigEmpresa config = ConfigEmpresa.builder()
            .geolocAtiva(true)
            .latCentro(null) // Sem coordenadas configuradas
            .longCentro(null)
            .raioGeoloc(50)
            .build();

        when(registroPontoRepository.findLastTypeRegistration(anyLong())).thenReturn(null);
        when(configEmpresaService.findConfigsEmpresa()).thenReturn(new DataConfigEmpresa(config));

        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);
        request.setLatitude(-12.2661);
        request.setLongitude(-38.9670);

        assertThrows(IllegalStateException.class,
            () -> registroPontoService.registrarPonto(request, this.request));
    }
}