package com.marcweb.exceptions;

public class AssinaturaDigitalException extends Exception {
    public AssinaturaDigitalException(String message) {
        super(message);
    }

    public AssinaturaDigitalException(String message, Throwable cause) {
        super(message, cause);
    }
}
