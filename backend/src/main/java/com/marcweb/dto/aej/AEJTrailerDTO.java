package com.marcweb.dto.aej;

import lombok.Data;

@Data
public class AEJTrailerDTO {
    private String tipoReg; // 2 N - "99"
    private Integer qtRegistrosTipo01; // 1-9 N - Quantidade registros tipo 01
    private Integer qtRegistrosTipo02; // 1-9 N - Quantidade registros tipo 02
    private Integer qtRegistrosTipo03; // 1-9 N - Quantidade registros tipo 03
    private Integer qtRegistrosTipo04; // 1-9 N - Quantidade registros tipo 04
    private Integer qtRegistrosTipo05; // 1-9 N - Quantidade registros tipo 05
    private Integer qtRegistrosTipo06; // 1-9 N - Quantidade registros tipo 06
    private Integer qtRegistrosTipo07; // 1-9 N - Quantidade registros tipo 07
    private Integer qtRegistrosTipo08; // 1-9 N - Quantidade registros tipo 08
}
