package com.marcweb.exceptions;

public class FolhaPontoDuplicadaException extends RuntimeException {
    public FolhaPontoDuplicadaException() {
        super("Erro, já existe uma folha de ponto para esse mes");
    }
}
