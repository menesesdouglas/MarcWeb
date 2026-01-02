package com.marcweb.exceptions;

import com.marcweb.entity.TipoRegistro;

public class RegistroPontoSequenciaInvalidaException extends RuntimeException {
    public RegistroPontoSequenciaInvalidaException(TipoRegistro tipoRegistro) {
        super("Erro antes você precisa  bater o ponto de " + tipoRegistro.getText());
    }
}
