package com.marcweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para a resposta de login
@Data // Gera getters, setters, equals, hashCode, toString (Lombok)
@Builder // Gera um builder (Lombok)
@NoArgsConstructor // Gera construtor sem argumentos (Lombok)
@AllArgsConstructor // Gera construtor com todos os argumentos (Lombok)
public class LoginResponse {
    private String token; // O token JWT gerado
    private String tokenType = "Bearer"; // Tipo do token (padrão Bearer)
    private Long userId; // ID do usuário autenticado (opcional, útil para o frontend)
    private String username; // Nome de usuário autenticado (opcional, útil para o frontend)
    private String role; // Papel (role) do usuário autenticado (opcional, útil para o frontend)
}