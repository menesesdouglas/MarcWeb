package com.marcweb.controller;

import com.marcweb.exceptions.UsernameDuplicatedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerException {


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UsernameDuplicatedException.class)
    public String handlerUsernameDuplicatedException(UsernameDuplicatedException e){
        return e.getMessage();
    }
}
