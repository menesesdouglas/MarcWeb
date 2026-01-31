package com.marcweb.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Nome de usuário é obrigatório")
    @Size(min = 3, max = 50, message = "Nome de usuário deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Nome de usuário deve conter apenas letras, números, traços e underscores")
    private String username; // Nome de usuário fornecido pelo cliente

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    private String password; // Senha fornecida pelo cliente
}
