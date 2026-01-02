package com.marcweb.exceptions;

public class ConfigEmpresaNotFoundException extends RuntimeException {
    public ConfigEmpresaNotFoundException() {
        super("Sem registro correspondente");
    }
}
