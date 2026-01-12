package com.marcweb.dto.folha_ponto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Periodo {

    private int mes;
    private int ano;
    private int dias_uteis;
    private int dias_trabalhados;
    private int dias_falta;
    private int dias_folga;


    public Periodo(int mes, int ano, int dias_trabalhados) {
        this.mes = mes;
        this.ano = ano;
        this.dias_trabalhados = dias_trabalhados;
    }
}
