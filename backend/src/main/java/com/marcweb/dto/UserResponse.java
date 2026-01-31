// src/main/java/com/marcweb/dto/UserResponse.java
package com.marcweb.dto;

import com.marcweb.entity.Role;
import com.marcweb.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data // Gera getters, setters, equals, hashCode, toString
public class UserResponse {

    private Long id;
    private String username;
    private String nome;
    private Role role; // Usando o enum Role
    private Boolean ativo;
    private Boolean consentGeoloc;
    private String cargo;
    private String cpf;
    private LocalDateTime createdAt;
    // private LocalDateTime updatedAt; // Removido, pois não existe na entidade User

    // Construtor padrão (necessário para frameworks como Jackson)
    public UserResponse() {}

    // Construtor a partir da entidade User
    public UserResponse(User user) {
        if(user != null) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.nome = user.getNome();
            this.role = user.getRole();
            this.ativo = user.getAtivo();
            this.consentGeoloc = user.getConsentGeoloc();
            this.cargo = user.getCargo();
            this.cpf = user.getCpf();
            this.createdAt = user.getCreatedAt();
        }
    }
}