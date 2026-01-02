package com.marcweb.entity;

import lombok.Getter;

@Getter
public enum TipoRegistro {
    ENTRADA("Entrada"),
    INICIO_DESCANSO("Inicio descanso"),
    FIM_DESCANSO("Fim descanso"),
    SAIDA("Saída");

    private final String text;

    TipoRegistro(String text) {
        this.text = text;
    }
}