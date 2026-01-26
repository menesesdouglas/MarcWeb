package com.marcweb.dto;

import com.marcweb.entity.TipoRegistro;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegistroPontoResponse {
    private TipoRegistro tipo;
    private String nomeUsuario;
    private String username;
    private String ip;
    private String justificativa;
    private LocalDateTime timestamp;

}
