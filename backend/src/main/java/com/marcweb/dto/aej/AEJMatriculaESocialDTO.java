package com.marcweb.dto.aej;

import lombok.Data;

@Data
public class AEJMatriculaESocialDTO {
    private String tipoReg; // 2 N - "06"
    private String idtVinculoAej; // 1-9 N - Identificador do vínculo
    private String matEsocial; // 1-30 A - Matrícula do vínculo no eSocial
}
