package com.marcweb.exceptions;

public class UsuarioBloqueadoException extends RuntimeException {
    public UsuarioBloqueadoException() {
        super("Este usuário excedeu o limite de ativações");
    }
}
