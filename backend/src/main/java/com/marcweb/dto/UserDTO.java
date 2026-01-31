package com.marcweb.dto;

import lombok.Data;

@Data
public class UserDTO {
    private final String name;
    private final String role;
    private final String status;
    private final String cpf;

    public UserDTO(String name, String role, boolean ativo, String cpf) {
        this.name = name;
        this.role = role;
        this.status = ativo? "Ativo" : "Inativo";
        this.cpf = cpf;
    }
}
