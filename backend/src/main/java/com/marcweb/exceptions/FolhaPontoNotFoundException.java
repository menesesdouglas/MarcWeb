package com.marcweb.exceptions;

public class FolhaPontoNotFoundException extends RuntimeException {
    public FolhaPontoNotFoundException() {
        super("Folha de ponto não encontrada para operiodo informado, tente gera-la primeiro.");
    }
}
