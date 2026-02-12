package com.marcweb.dto.aej;

import lombok.Data;

@Data
public class AEJPtrpDTO {
    private String tipoReg; // 2 N - "08"
    private String nomeProg; // 1-150 A - Nome do PTRP
    private String versaoProg; // 1-8 A - Versão do PTRP
    private Integer tpIdtDesenv; // 1 N - Tipo identificador desenvolvedor: 1 CNPJ, 2 CPF
    private String idtDesenv; // 11 or 14 N - CNPJ or CPF do desenvolvedor
    private String razaoNomeDesenv; // 1-150 A - Razão social or nome do desenvolvedor
    private String emailDesenv; // 1-50 A - E-mail do desenvolvedor
}
