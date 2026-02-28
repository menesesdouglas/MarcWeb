package com.marcweb.entity;

import lombok.Getter;

@Getter
public enum Plano {
    FREEMIUM("Freemium"),
    BASICO("Básico"),
    PREMIUM("Premium"),
    ENTERPRISE("Enterprise");

    private final String plano;

    Plano(String plano) {
        this.plano = plano;
    }
}
