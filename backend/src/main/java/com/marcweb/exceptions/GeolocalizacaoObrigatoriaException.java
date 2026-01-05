package com.marcweb.exceptions;

public class GeolocalizacaoObrigatoriaException extends RuntimeException {
    public GeolocalizacaoObrigatoriaException(String message) {
        super(message);
    }
}