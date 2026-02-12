package com.marcweb.dto.aej;

import lombok.Data;

@Data
public class AEJVinculoDTO {
    private String tipoReg; // 2 N - "03"
    private String idtVinculoAej; // 1-9 N - Identificador do vínculo no AEJ
    private String cpf; // 11 N - CPF do empregado
    private String nomeEmp; // 1-150 A - Nome do empregado
}
