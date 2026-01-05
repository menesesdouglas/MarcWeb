package com.marcweb.exceptions;

public class FeriadoAnoInvalidoException extends RuntimeException {
    public FeriadoAnoInvalidoException() {
        super("O feriado precisa ser do ano atual");
    }
}
