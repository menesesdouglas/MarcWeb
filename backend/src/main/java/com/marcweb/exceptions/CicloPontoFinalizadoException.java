package com.marcweb.exceptions;

public class CicloPontoFinalizadoException extends RuntimeException {
    public CicloPontoFinalizadoException() {
        super("Você já finalizou o ciclo diário de pontos");
    }
}
