package com.marcweb.exceptions;

public class RegistroEmFolgaException extends RuntimeException {
    public RegistroEmFolgaException() {
        super("Não é permitido registrar ponto durante uma folga");
    }
}
