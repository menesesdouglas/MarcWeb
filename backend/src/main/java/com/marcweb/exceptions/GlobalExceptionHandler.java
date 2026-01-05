package com.marcweb.exceptions; // ou o pacote que você usar para handlers/configs

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Aplica a todos os @RestController
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UsernameDuplicatedException.class)
    public String handlerUsernameDuplicatedException(UsernameDuplicatedException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UsernameNotFoundException.class)
    public String handlerUsernameNotFoundException(UsernameNotFoundException e) {
        log.error(e.getMessage());
        return e.getMessage();
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ConfigEmpresaNotFoundException.class)
    public String handlerConfigEmpresaNotFoundException(ConfigEmpresaNotFoundException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroPontoSequenciaInvalidaException.class)
    public String handlerRegistroPontoSequenciaInvalidaException(RegistroPontoSequenciaInvalidaException e) {
        log.warn("Erro na ordem dos registros de ponto");
        return e.getMessage();
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FeriadoAnoInvalidoException.class)
    public String handlerFeriadoAnoInvalidoException(FeriadoAnoInvalidoException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(FeriadoDuplicadoException.class)
    public String handlerFeriadoDuplicadoException(FeriadoDuplicadoException e) { // Corrigido nome do método
        log.warn(e.getMessage());
        return e.getMessage();
    }

    // --- NOVAS EXCEÇÕES PARA GEOLOCALIZAÇÃO ---
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(GeolocalizacaoObrigatoriaException.class)
    public String handlerGeolocalizacaoObrigatoriaException(GeolocalizacaoObrigatoriaException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroForaDoRaioException.class)
    public String handlerRegistroForaDoRaioException(RegistroForaDoRaioException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }
    // --- FIM DAS NOVAS EXCEÇÕES ---

    /*@ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor.");
    }*/
}