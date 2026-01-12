package com.marcweb.dto.folha_ponto;

import com.marcweb.entity.RegistroPonto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dia {

    private LocalDate data;
    private String dia_semana;
    private String tipo_dia;
    private List<RegistroPontoDTO> registros;
    private String horas_trabalhadas;
    private String horas_extras;
    private String observacoes;


    ///  CRIANDO OBJETO A PARTIR DE UMA LISTA DE REGISTROS DE PONTO
    /// QUE FORAM BATIDOS EM UM DIAS ESPECIFICO
    public Dia(List<RegistroPonto> registrosPontoDeUmDia, BigDecimal horasTrabalhadas) {
        this.data = registrosPontoDeUmDia.get(0).getTimestamp().toLocalDate();
        this.registros = registrosPontoDeUmDia.stream().map(RegistroPontoDTO::new).toList();
        this.dia_semana = this.data.getDayOfWeek().name();
        this.tipo_dia = this.dia_semana.equals("SABADO OU DOMINGO") ? "Dia útil" : "Final de semana"; /// VOU MODIFCAR DEPOIS
        this.horas_trabalhadas = horasTrabalhadas.toString();
        this.horas_extras = 0 + ""; /// VOU ALTERAR DEPOIS
        this.observacoes = "Dia normal de trabalho"; ///
    }


    /// CRIANDO OBJETO A PARTIR DE UMA DATA RELACIONADA COM FOLGA
    public Dia(LocalDate dataFolga) {
        this.data = dataFolga;
        this.dia_semana = dataFolga.getDayOfWeek().name();
        this.tipo_dia = this.dia_semana.equals("SABADO OU DOMINGO") ? "Dia útil" : "Final de semana"; // VOU MODIFCAR DEPOIS
        this.observacoes = "Dia de folga";
        this.horas_extras = 0 + "";
        this.horas_trabalhadas = 0 + "";
        this.registros = List.of(); /// LISTA VÁZIA
    }
}
