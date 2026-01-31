package com.marcweb.dto.ponto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPontoResponseWithWarning {
    private String message;
    private boolean hasIncompletePreviousDay;
    private String warningMessage;

    public RegistroPontoResponseWithWarning(String message, boolean hasIncompletePreviousDay) {
        this.message = message;
        this.hasIncompletePreviousDay = hasIncompletePreviousDay;
        this.warningMessage = hasIncompletePreviousDay ?
            "ATENÇÃO: Você tem um registro de ponto incompleto do dia anterior. Por favor, contate seu supervisor para regularizar a situação." :
            null;
    }
}