package com.marcweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.dto.ponto.CreateRegistroPonto;
import com.marcweb.dto.login.LoginRequest;
import com.marcweb.dto.login.LoginResponse;
import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.Role;
import com.marcweb.entity.TipoRegistro;
import com.marcweb.entity.User;
import com.marcweb.repository.ConfigEmpresaRepository;
import com.marcweb.repository.RegistroPontoRepository;
import com.marcweb.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistroPontoDTOControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private ConfigEmpresaRepository configEmpresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String employeeToken;

    @BeforeEach
    void setup() throws Exception {
        // Limpar dados
        registroPontoRepository.deleteAll();
        userRepository.deleteAll();
        configEmpresaRepository.deleteAll();

        // Criar configuração padrão
        ConfigEmpresa config = ConfigEmpresa.builder()
                .nomeEmpresa("Test Company")
                .latCentro(-12.2658)
                .longCentro(-38.9668)
                .raioGeoloc(100)
                .geolocAtiva(false)
                .build();
        configEmpresaRepository.save(config);

        // Criar usuário de teste
        User user = User.builder()
                .username("test.user")
                .nome("Test User")
                .password(passwordEncoder.encode("test123"))
                .role(Role.EMPLOYEE)
                .ativo(true)
                .build();
        userRepository.save(user);

        // Obter token
        LoginRequest loginRequest = new LoginRequest("test.user", "test123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        employeeToken = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class).getToken();
    }

    @Test
    void registrarPonto_FirstEntrySuccess() throws Exception {
        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);

        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void registrarPonto_InvalidFirstEntry() throws Exception {
        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.INICIO_DESCANSO);

        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Erro antes você precisa bater o ponto de Entrada"));
    }

    @Test
    void registrarPonto_CompleteSequence() throws Exception {
        // ENTRADA
        CreateRegistroPonto entrada = new CreateRegistroPonto();
        entrada.setTipo(TipoRegistro.ENTRADA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated());

        // INICIO_DESCANSO
        CreateRegistroPonto inicioDescanso = new CreateRegistroPonto();
        inicioDescanso.setTipo(TipoRegistro.INICIO_DESCANSO);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inicioDescanso)))
                .andExpect(status().isCreated());

        // FIM_DESCANSO
        CreateRegistroPonto fimDescanso = new CreateRegistroPonto();
        fimDescanso.setTipo(TipoRegistro.FIM_DESCANSO);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fimDescanso)))
                .andExpect(status().isCreated());

        // SAIDA
        CreateRegistroPonto saida = new CreateRegistroPonto();
        saida.setTipo(TipoRegistro.SAIDA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saida)))
                .andExpect(status().isCreated());
    }

    @Test
    void registrarPonto_InvalidSequence() throws Exception {
        // ENTRADA
        CreateRegistroPonto entrada = new CreateRegistroPonto();
        entrada.setTipo(TipoRegistro.ENTRADA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated());

        // Tentar SAIDA diretamente após ENTRADA (sem descanso)
        CreateRegistroPonto saida = new CreateRegistroPonto();
        saida.setTipo(TipoRegistro.SAIDA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void listarRegistros_Success() throws Exception {
        // Criar alguns registros
        User user = userRepository.findByUsernameIgnoreCase("test.user").orElseThrow();

        RegistroPonto registro1 = new RegistroPonto();
        registro1.setUser(user);
        registro1.setTipo(TipoRegistro.ENTRADA);
        registro1.setTimestamp(java.time.LocalDateTime.now().minusHours(2));

        RegistroPonto registro2 = new RegistroPonto();
        registro2.setUser(user);
        registro2.setTipo(TipoRegistro.SAIDA);
        registro2.setTimestamp(java.time.LocalDateTime.now().minusHours(1));

        registroPontoRepository.save(registro1);
        registroPontoRepository.save(registro2);

        // Listar registros
        mockMvc.perform(get("/api/ponto/me")
                        .header("Authorization", "Bearer " + employeeToken)
                        .param("start", "2026-01-01T00:00:00")
                        .param("end", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void registrarPonto_WithoutAuthentication() throws Exception {
        CreateRegistroPonto request = new CreateRegistroPonto();
        request.setTipo(TipoRegistro.ENTRADA);

        mockMvc.perform(post("/api/ponto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}