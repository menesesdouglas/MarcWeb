package com.marcweb.dto.folha_ponto;

import com.marcweb.entity.RegistroPonto;
import com.marcweb.entity.TipoRegistro;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistroPontoDTO {
    private TipoRegistro tipo;
    private String ip;
    private String justificativa;
    private LocalDateTime timestamp;
    private Double latitude;
    private Double longitude;

    public RegistroPontoDTO(RegistroPonto registroPonto) {
        BeanUtils.copyProperties(registroPonto, this);
    }
}
