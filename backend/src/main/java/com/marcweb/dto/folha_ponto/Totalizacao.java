package com.marcweb.dto.folha_ponto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Totalizacao {

    // Stored in minutes for proper hours:minutes formatting
    private BigDecimal horas_trabalhadas; // in minutes
    private int horas_extras;             // in minutes
    private int horas_debito;             // in minutes
    private int saldo_horas;              // in minutes
}
