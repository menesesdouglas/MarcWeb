package com.marcweb.dto.aej;

import lombok.Data;

@Data
public class AEJRepDTO {
    private String tipoReg; // 2 N - "02"
    private String idRepAej; // 1-9 N - Identificador do REP no AEJ
    private Integer tpRep; // 1 N - Tipo do REP: 1: REP-C, 2: REP-A, 3: REP-P
    private String nrRep; // 17 N - Número de fabricação or identificador
}
