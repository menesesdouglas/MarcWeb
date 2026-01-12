package com.marcweb.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.dto.CreateRegistroPonto;
import com.marcweb.dto.LoginRequest;
import com.marcweb.dto.LoginResponse;
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
class RegistroPontoDTOIntegrationTest {

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
    void completeWorkDayFlow() throws Exception {
        // ENTRADA - 08:00
        CreateRegistroPonto entrada = new CreateRegistroPonto();
        entrada.setTipo(TipoRegistro.ENTRADA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated());

        // INICIO_DESCANSO - 12:00
        CreateRegistroPonto inicioDescanso = new CreateRegistroPonto();
        inicioDescanso.setTipo(TipoRegistro.INICIO_DESCANSO);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inicioDescanso)))
                .andExpect(status().isCreated());

        // FIM_DESCANSO - 13:00
        CreateRegistroPonto fimDescanso = new CreateRegistroPonto();
        fimDescanso.setTipo(TipoRegistro.FIM_DESCANSO);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fimDescanso)))
                .andExpect(status().isCreated());

        // SAIDA - 17:00
        CreateRegistroPonto saida = new CreateRegistroPonto();
        saida.setTipo(TipoRegistro.SAIDA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saida)))
                .andExpect(status().isCreated());

        // Verificar registros
        mockMvc.perform(get("/api/ponto/me")
                        .header("Authorization", "Bearer " + employeeToken)
                        .param("start", "2026-01-01T00:00:00")
                        .param("end", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void invalidSequence_ShouldFail() throws Exception {
        // Tentar INICIO_DESCANSO como primeiro registro
        CreateRegistroPonto inicioDescanso = new CreateRegistroPonto();
        inicioDescanso.setTipo(TipoRegistro.INICIO_DESCANSO);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inicioDescanso)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void multipleWorkCycles() throws Exception {
        // Primeiro ciclo
        CreateRegistroPonto entrada1 = new CreateRegistroPonto();
        entrada1.setTipo(TipoRegistro.ENTRADA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada1)))
                .andExpect(status().isCreated());

        CreateRegistroPonto saida1 = new CreateRegistroPonto();
        saida1.setTipo(TipoRegistro.SAIDA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saida1)))
                .andExpect(status().isCreated());

        // Segundo ciclo
        CreateRegistroPonto entrada2 = new CreateRegistroPonto();
        entrada2.setTipo(TipoRegistro.ENTRADA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada2)))
                .andExpect(status().isCreated());

        CreateRegistroPonto saida2 = new CreateRegistroPonto();
        saida2.setTipo(TipoRegistro.SAIDA);
        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saida2)))
                .andExpect(status().isCreated());

        // Verificar registros
        mockMvc.perform(get("/api/ponto/me")
                        .header("Authorization", "Bearer " + employeeToken)
                        .param("start", "2026-01-01T00:00:00")
                        .param("end", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void registroWithJustificativa() throws Exception {
        CreateRegistroPonto entrada = new CreateRegistroPonto();
        entrada.setTipo(TipoRegistro.ENTRADA);
        entrada.setJustificativa("Cheguei atrasado devido a problemas de transporte");

        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated());
    }

    @Test
    void registroWithGeolocalizacao() throws Exception {
        CreateRegistroPonto entrada = new CreateRegistroPonto();
        entrada.setTipo(TipoRegistro.ENTRADA);
        entrada.setLatitude(-12.2661);
        entrada.setLongitude(-38.9670);
        entrada.setPrecisao(10.0);

        mockMvc.perform(post("/api/ponto")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated());
    }
}