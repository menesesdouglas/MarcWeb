package com.marcweb.dto.folha_ponto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Totalizacao {

    private BigDecimal horas_trabalhadas;
    private int horas_extras;
    private int horas_debito;
    private int saldo_horas;
}
