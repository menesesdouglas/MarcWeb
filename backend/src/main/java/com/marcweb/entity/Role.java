package com.marcweb.entity;

import lombok.Getter;

@Getter
public enum Role {
    EMPLOYEE("Usuário"),
    ADMIN("Administrador");

    private final String texto;

    Role(String texto) {
        this.texto = texto;
    }
}