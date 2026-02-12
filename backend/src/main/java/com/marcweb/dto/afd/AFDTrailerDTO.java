package com.marcweb.dto.afd;

import lombok.Data;

@Data
public class AFDTrailerDTO {
    private String nsr; // 001-009 9 N "999999999"
    private Integer qtdRegTipo2; // 010-018 9 N Quantidade registros tipo 2
    private Integer qtdRegTipo3; // 019-027 9 N Quantidade registros tipo 3
    private Integer qtdRegTipo4; // 028-036 9 N Quantidade registros tipo 4
    private Integer qtdRegTipo5; // 037-045 9 N Quantidade registros tipo 5
    private Integer qtdRegTipo6; // 046-054 9 N Quantidade registros tipo 6
    private Integer qtdRegTipo7; // 055-063 9 N Quantidade registros tipo 7
    private String tipoReg; // 064-064 1 N "9"
}
