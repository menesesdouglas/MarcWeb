package com.marcweb.exceptions; // ou o pacote que você usar para handlers/configs


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice // Aplica a todos os @RestController
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UsernameDuplicatedException.class)
    public Map<String, Object> handlerUsernameDuplicatedException(UsernameDuplicatedException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UsernameNotFoundException.class)
    public Map<String, Object> handlerUsernameNotFoundException(UsernameNotFoundException e) {
        log.error(e.getMessage());
        return errorObject(e.getMessage());
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ConfigEmpresaNotFoundException.class)
    public Map<String, Object> handlerConfigEmpresaNotFoundException(ConfigEmpresaNotFoundException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroPontoSequenciaInvalidaException.class)
    public Map<String, Object> handlerRegistroPontoSequenciaInvalidaException(RegistroPontoSequenciaInvalidaException e) {
        log.warn("Erro na ordem dos registros de ponto");
        return errorObject(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, Object> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Erro no preenchimento do request body");
        List<FieldError> erros = e.getFieldErrors();
        StringBuilder message = new StringBuilder();
        for (FieldError fieldError : erros) {
            message.append(fieldError.getDefaultMessage()).append(", ");
        }
        return errorObject(message.toString());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CicloPontoFinalizadoException.class)
    public Map<String, Object> handlerCicloPontoFinalizadoException(CicloPontoFinalizadoException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PeriodoFolhaPontoException.class)
    public Map<String, Object> handlerPeriodoFolhaPontoException(PeriodoFolhaPontoException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FeriadoAnoInvalidoException.class)
    public Map<String, Object> handlerFeriadoAnoInvalidoException(FeriadoAnoInvalidoException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalStateException.class)
    public Map<String, Object> handlerIllegalStateException(IllegalStateException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(FeriadoDuplicadoException.class)
    public Map<String, Object> handlerFeriadoDuplicadoException(FeriadoDuplicadoException e) { // Corrigido nome do método
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SemRegistrosException.class)
    public Map<String, Object> handlerSemRegistrosException(SemRegistrosException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroEmFolgaException.class)
    public Map<String, Object> handlerRegistroEmFolgaException(RegistroEmFolgaException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }

    // --- NOVAS EXCEÇÕES PARA DIA ÚTIL ---
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroEmDomingoException.class)
    public Map<String, Object> handlerRegistroEmDomingoException(RegistroEmDomingoException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroEmFeriadoException.class)
    public Map<String, Object> handlerRegistroEmFeriadoException(RegistroEmFeriadoException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }


    // --- FIM DAS NOVAS EXCEÇÕES ---

    // --- EXCEÇÕES GEOLOCALIZAÇÃO (já implementadas anteriormente) ---
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(GeolocalizacaoObrigatoriaException.class)
    public Map<String, Object> handlerGeolocalizacaoObrigatoriaException(GeolocalizacaoObrigatoriaException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistroForaDoRaioException.class)
    public Map<String, Object> handlerRegistroForaDoRaioException(RegistroForaDoRaioException e) {
        log.warn(e.getMessage());
        return errorObject(e.getMessage());
    }
    // --- FIM EXCEÇÕES GEOLOCALIZAÇÃO ---

    /// PARA PADRONIZAR A RESPOSTA DE ERRO
    private Map<String, Object> errorObject(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("erro", message);
        return map;
    }
}