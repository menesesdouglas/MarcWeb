package com.marcweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.dto.TimeAdjustmentRequest;
import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.TipoRegistro;
import com.marcweb.entity.User;
import com.marcweb.repository.RegistroPontoRepository;
import com.marcweb.repository.UserRepository;
import com.marcweb.service.RegistroPontoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TimeAdjustmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setNome("Test User");
        testUser.setPassword("password");
        testUser.setRole(com.marcweb.entity.Role.EMPLOYEE);
        testUser.setAtivo(true);
        userRepository.save(testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void ajustarPonto_DeveCriarAjusteComSucesso() throws Exception {
        TimeAdjustmentRequest request = new TimeAdjustmentRequest(
            testUser.getId(),
            LocalDate.now(),
            "Entrada",
            LocalTime.of(8, 0),
            "Ajuste de teste"
        );

        mockMvc.perform(post("/api/ponto/ajuste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Ajuste de ponto realizado com sucesso"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void ajustarPonto_DeveLancarErro_QuandoUsuarioNaoExistir() throws Exception {
        TimeAdjustmentRequest request = new TimeAdjustmentRequest(
            9999L, // Non-existent user
            LocalDate.now(),
            "Entrada",
            LocalTime.of(8, 0),
            "Ajuste de teste"
        );

        mockMvc.perform(post("/api/ponto/ajuste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE") // Not ADMIN
    void ajustarPonto_DeveLancarErro_QuandoUsuarioNaoForAdmin() throws Exception {
        TimeAdjustmentRequest request = new TimeAdjustmentRequest(
            testUser.getId(),
            LocalDate.now(),
            "Entrada",
            LocalTime.of(8, 0),
            "Ajuste de teste"
        );

        mockMvc.perform(post("/api/ponto/ajuste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}