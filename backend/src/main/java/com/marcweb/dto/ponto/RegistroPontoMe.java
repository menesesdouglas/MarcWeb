package com.marcweb.dto.ponto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegistroPontoMe {
    List<RegistroPontoResponse> registros;
    private long minutosTrabalhados;
    private int monthLength;
}
