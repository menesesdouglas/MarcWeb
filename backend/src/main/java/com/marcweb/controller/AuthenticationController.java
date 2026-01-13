package com.marcweb.controller;

import com.marcweb.dto.CreateUser;
import com.marcweb.dto.LoginRequest;
import com.marcweb.dto.LoginResponse;
import com.marcweb.entity.User;
import com.marcweb.security.JwtService;
import com.marcweb.service.LogAuditoriaService;
import com.marcweb.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController // Marca como um controlador REST
@RequestMapping("/api/auth") // Define o prefixo comum para os endpoints deste controlador
@RequiredArgsConstructor // Gera construtor com campos final (Lombok)
public class AuthenticationController {

    private final AuthenticationManager authenticationManager; // Injeta o gerenciador de autenticação
    private final JwtService jwtService; // Injeta o serviço JWT
    private final UserService userService;
    private final LogAuditoriaService logAuditoriaService;

    @PostMapping("/login") // Mapeia requisições POST para /api/auth/login
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) { // Recebe LoginRequest no body
        // Cria um token de autenticação com username e password do request
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), // Username do request
                        request.getPassword()   // Password do request
                )
        );

        // Se a autenticação for bem-sucedida, o principal será o objeto User (UserDetails)
        var user = (User) authentication.getPrincipal();
        // Gera o token JWT para o usuário autenticado
        String jwt = jwtService.generateToken(user);

        // Constroi o DTO de resposta com o token e informações do usuário
        var loginResponse = LoginResponse.builder()
                .token(jwt) // O JWT gerado
                .userId(user.getId()) // ID do usuário
                .username(user.getUsername()) // Nome de usuário
                .role(user.getRole().name()) // Papel do usuário (convertido para string)
                .build();

        // Retorna o DTO de resposta com status 200 OK
        return ResponseEntity.ok(loginResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public void createUser(@RequestBody @Valid CreateUser data) {
        userService.createUser(data);
    }
}
