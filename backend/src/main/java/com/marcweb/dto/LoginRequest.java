package com.marcweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para a requisição de login
@Data // Gera getters, setters, equals, hashCode, toString (Lombok)
@Builder // Gera um builder (Lombok)
@NoArgsConstructor // Gera construtor sem argumentos (Lombok)
@AllArgsConstructor // Gera construtor com todos os argumentos (Lombok)
public class LoginRequest {
    private String username; // Nome de usuário fornecido pelo cliente
    private String password; // Senha fornecida pelo cliente
}