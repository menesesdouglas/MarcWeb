package com.marcweb.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcweb.dto.LoginRequest;
import com.marcweb.dto.LoginResponse;
import com.marcweb.entity.User;
import com.marcweb.repository.UserRepository;
import com.marcweb.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setup() {
        // Limpar usuários antes de cada teste
        userRepository.deleteAll();

        // Criar usuário admin padrão para testes
        User admin = User.builder()
                .username("admin")
                .nome("Administrador Teste")
                .password(passwordEncoder.encode("admin123"))
                .role(com.marcweb.entity.Role.ADMIN)
                .ativo(true)
                .build();
        userRepository.save(admin);
    }

    @Test
    void loginAdmin_Success() throws Exception {
        LoginRequest request = new LoginRequest("admin", "admin123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void loginEmployee_Success() throws Exception {
        // Criar funcionário para teste
        User employee = User.builder()
                .username("employee")
                .nome("Funcionário Teste")
                .password(passwordEncoder.encode("employee123"))
                .role(com.marcweb.entity.Role.EMPLOYEE)
                .ativo(true)
                .build();
        userRepository.save(employee);

        LoginRequest request = new LoginRequest("employee", "employee123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrong_password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithoutCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_NonexistentUser() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}