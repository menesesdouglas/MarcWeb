package com.marcweb.dto;

import lombok.Data;

@Data
public class UserDTO {
    private final String name;
    private final String role;
    private final String status;

    public UserDTO(String name, String role, boolean ativo) {
        this.name = name;
        this.role = role;
        this.status = ativo? "Ativo" : "Inativo";
    }
}
