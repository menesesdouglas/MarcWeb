package com.marcweb.exceptions;

public class FolhaPontoDuplicadaException extends RuntimeException {
    public FolhaPontoDuplicadaException() {
        super("Já existe uma folha de ponto para esse período");
    }
}
