package com.marcweb.exceptions;

public class ConformidadeException extends Exception {
    public ConformidadeException(String message) {
        super(message);
    }

    public ConformidadeException(String message, Throwable cause) {
        super(message, cause);
    }
}
