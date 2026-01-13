package com.marcweb.exceptions;

public class AEJException extends Exception {
    public AEJException(String message) {
        super(message);
    }

    public AEJException(String message, Throwable cause) {
        super(message, cause);
    }
}
