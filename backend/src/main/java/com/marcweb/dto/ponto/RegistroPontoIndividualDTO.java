package com.marcweb.dto.ponto;

import com.marcweb.entity.TipoRegistro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPontoIndividualDTO {
    
    private Long id;
    private String nomeUsuario;
    private String cpf;
    private String cargo;
    private String tenantNome;
    private String tenantCnpj;
    private TipoRegistro tipo;
    private LocalDateTime timestamp;
    private Double latitude;
    private Double longitude;
    private Double precisao;
    private String ip;
    private String justificativa;
    private String expedienteId;
    private LocalDateTime dataEmissao;
}