package com.marcweb.exceptions;

public class FeriadoDuplicadoException extends RuntimeException {
    public FeriadoDuplicadoException() {
        super("Já existe um feriado para essa data");
    }
}
