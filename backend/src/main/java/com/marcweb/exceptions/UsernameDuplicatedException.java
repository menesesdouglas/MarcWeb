package com.marcweb.exceptions;

public class UsernameDuplicatedException extends RuntimeException {
    public UsernameDuplicatedException(String username) {
        super("O username " + username + " já está sendo utilizado");
    }
}
