package com.marcweb.exceptions;

public class UsernameDuplicatedException extends RuntimeException {
    public UsernameDuplicatedException() {
        super("Este username já está sendo utilizado");
    }
}
