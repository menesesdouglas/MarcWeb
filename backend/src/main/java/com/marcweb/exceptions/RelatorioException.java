package com.marcweb.exceptions;

public class RelatorioException extends Exception {
    public RelatorioException(String message) {
        super(message);
    }

    public RelatorioException(String message, Throwable cause) {
        super(message, cause);
    }
}
