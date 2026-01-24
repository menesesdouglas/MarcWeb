package com.marcweb.controller;

import com.marcweb.dto.CreateUser;
import com.marcweb.dto.LoginRequest;
import com.marcweb.dto.LoginResponse;
import com.marcweb.entity.User;
import com.marcweb.security.JwtService;
import com.marcweb.service.LogAuditoriaService;
import com.marcweb.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController // Marca como um controlador REST
@RequestMapping("/api/auth") // Define o prefixo comum para os endpoints deste controlador
@RequiredArgsConstructor // Gera construtor com campos final (Lombok)
public class AuthenticationController {

    private final JwtService jwtService; // Injeta o serviço JWT
    private final UserService userService;



    @PostMapping("/login") // Mapeia requisições POST para /api/auth/login
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) { // Recebe LoginRequest no body
        // Cria um token de autenticação com username e password do request
        LoginResponse loginResponse = userService.logarUsuario(request);
        // Retorna o DTO de resposta com status 200 OK
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Endpoint para renovar o token JWT antes da expiração
     * @param request Objeto HttpServletRequest para acessar o token
     * @param userDetails Detalhes do usuário autenticado
     * @return ResponseEntity com novo token
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        // Verifica se o token precisa ser renovado
        if (!jwtService.needsRefresh(token)) {
            return ResponseEntity.ok().build(); // Token ainda válido
        }

        // Gera novo token para o usuário autenticado
        User user = userService.findByUsername(userDetails.getUsername());
        String newToken = jwtService.generateToken(user);

        LoginResponse response = LoginResponse.builder()
                .token(newToken)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public void createUser(@RequestBody @Valid CreateUser data) {
        userService.createUser(data);
    }
}
