package com.marcweb.exceptions; // ou o pacote que você usar para handlers/configs

import com.marcweb.exceptions.RegistroPontoSequenciaInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Aplica a todos os @RestController
public class GlobalExceptionHandler {

    @ExceptionHandler(RegistroPontoSequenciaInvalidaException.class)
    public ResponseEntity<String> handleRegistroPontoSequenciaInvalida(RegistroPontoSequenciaInvalidaException ex) {
        // Retorna 400 Bad Request com mensagem da exceção
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /*@ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor.");
    }*/
}