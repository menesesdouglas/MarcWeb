package com.marcweb.service;

import com.marcweb.dto.CreateRegistroPonto;
import com.marcweb.dto.RegistroPontoResponse;
import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.TipoRegistro;
import com.marcweb.entity.User;
import com.marcweb.exceptions.RegistroPontoSequenciaInvalidaException;
import com.marcweb.repository.RegistroPontoRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RegistroPontoService {

    private final UserService userService;
    private final RegistroPontoRepository repository;

    @Transactional
    public void registrarPonto(CreateRegistroPonto data, HttpServletRequest request) {
        // COLETANDO O USUÁRIO NO CONTEXTO DE SEGURANÇA
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        User user = userService.findByUsername(userDetails.getUsername());

        // VALIDAÇÃO DE REGISTRO
        validarRegistro(data.getTipo(), user.getId());

        // SALVANDO CASO PASSE NA VALIDAÇÃO
        repository.save(new RegistroPonto(data, user, getIpUser(request)));
    }

    // VALIDANDO ORDEM DE TIPO DE REGISTRO DE PONTO
    private void validarRegistro(TipoRegistro tipoRegistro, Long idUsuario) {
        // BUSCANDO ULTIMO REGISTRO NO BANCO DE DADOS
        TipoRegistro ultimoTipoRegistro = repository.findLastTypeRegistration(idUsuario);

        // VERIFICAÇÃO PARA O PRIMEIRO REGISTRO DO USUÁRIO
        if (ultimoTipoRegistro == null) {
            // O primeiro registro DEVE ser uma ENTRADA
            if (tipoRegistro != TipoRegistro.ENTRADA) {
                // Lança exceção informando que o primeiro registro deve ser uma entrada
                throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.ENTRADA);
            }
            // Se for ENTRADA, é um registro válido, não precisa validar regras subsequentes
            return;
        }

        // VALIDAÇÃO DE ORDEM PARA REGISTROS SUBSEQUENTES (código original ajustado)
        if (ultimoTipoRegistro.equals(TipoRegistro.ENTRADA) && !tipoRegistro.equals(TipoRegistro.INICIO_DESCANSO))
            throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.INICIO_DESCANSO);

        if (ultimoTipoRegistro.equals(TipoRegistro.INICIO_DESCANSO) && !tipoRegistro.equals(TipoRegistro.FIM_DESCANSO))
            throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.FIM_DESCANSO);

        if (ultimoTipoRegistro.equals(TipoRegistro.FIM_DESCANSO) && !tipoRegistro.equals(TipoRegistro.SAIDA))
            throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.SAIDA);

        if (ultimoTipoRegistro.equals(TipoRegistro.SAIDA) && !tipoRegistro.equals(TipoRegistro.ENTRADA))
            throw new RegistroPontoSequenciaInvalidaException(TipoRegistro.ENTRADA);
    }

    public List<RegistroPontoResponse> buscarPontosRegistrados(LocalDateTime start, LocalDateTime end) {
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        User user = userService.findByUsername(userDetails.getUsername());
        return repository.findByUserAndTimestampBetweenOrderByTimestampAsc(user, start, end);
    }

    private String getIpUser(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        else ip = ip.split(",")[0];
        return ip;
    }
<<<<<<< Updated upstream
}
=======




}
>>>>>>> Stashed changes
