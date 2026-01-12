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


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(FolhaPontoDuplicadaException.class)
    public String handlerFolhaPontoDuplicadaException(FolhaPontoDuplicadaException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SemRegistrosException.class)
    public String handlerSemRegistrosException(SemRegistrosException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroEmFolgaException.class)
    public String handlerRegistroEmFolgaException(RegistroEmFolgaException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }

    // --- NOVAS EXCEÇÕES PARA DIA ÚTIL ---
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroEmDomingoException.class)
    public String handlerRegistroEmDomingoException(RegistroEmDomingoException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroEmFeriadoException.class)
    public String handlerRegistroEmFeriadoException(RegistroEmFeriadoException e) {
        log.warn(e.getMessage());
        return e.getMessage();
    }
    // --- FIM DAS NOVAS EXCEÇÕES ---

    // --- EXCEÇÕES GEOLOCALIZAÇÃO (já implementadas anteriormente) ---
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
    // --- FIM EXCEÇÕES GEOLOCALIZAÇÃO ---

}