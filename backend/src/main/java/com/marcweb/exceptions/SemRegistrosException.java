package com.marcweb.exceptions;

public class SemRegistrosException extends RuntimeException {
    public SemRegistrosException() {
        super("Não existem registros para o periodo informado");
    }
}
