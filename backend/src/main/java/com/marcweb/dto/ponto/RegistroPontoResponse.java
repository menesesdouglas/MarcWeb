package com.marcweb.dto.ponto;

import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.TipoRegistro;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegistroPontoResponse {
    private Long id;
    private TipoRegistro tipo;
    private String nomeUsuario;
    private String username;
    private String ip;
    private String justificativa;
    private LocalDateTime timestamp;
    private UUID idExpediente;

}
