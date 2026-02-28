package com.marcweb.exceptions;

import com.marcweb.entity.Plano;

public class LimiteFuncionarioAtivosException extends RuntimeException {
    public LimiteFuncionarioAtivosException(String message) {
        super(message);
    }
}
