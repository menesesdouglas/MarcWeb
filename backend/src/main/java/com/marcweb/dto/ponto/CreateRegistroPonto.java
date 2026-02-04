package com.marcweb.dto.ponto;

import com.marcweb.entity.TipoRegistro;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class CreateRegistroPonto {

    @NotNull(message = "Informe o tipo de registro")
    private TipoRegistro tipo; // ENTRADA, INICIO_DESCANSO, FIM_DESCANSO, SAIDA

    // TODO: Campos de geolocalização removidos temporariamente - mantidos para possível reativação futura
    // Campos de geolocalização (opcionais)
    // private Double latitude;
    // private Double longitude;
    // private Double precisao; // Precisão da geolocalização em metros
    
    private String justificativa;
}
