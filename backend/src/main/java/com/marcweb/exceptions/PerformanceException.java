package com.marcweb.exceptions;

public class PerformanceException extends Exception {
    public PerformanceException(String message) {
        super(message);
    }

    public PerformanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
