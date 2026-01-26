package com.marcweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegistroPontoMe {
    List<RegistroPontoResponse> registros;
    private long MinutosTrabalhados;
}
