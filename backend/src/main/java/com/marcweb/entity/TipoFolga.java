package com.marcweb.entity;

import lombok.Getter;

public enum TipoFolga {
    FERIAS("Ferias"),
    ATESTADO("Atestado"),
    FALTA_JUSTIFICADA("Falta justificada");

    @Getter
    private final String text;

    TipoFolga(String text) {
        this.text = text;
    }
}