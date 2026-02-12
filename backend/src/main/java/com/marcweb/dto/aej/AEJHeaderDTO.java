package com.marcweb.dto.aej;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AEJHeaderDTO {
    private String tipoReg; // 2 N - "01"
    private Integer tpIdtEmpregador; // 1 N - 1: CNPJ, 2: CPF
    private String idtEmpregador; // 11 or 14 N - CNPJ or CPF
    private String caepf; // 14 N - CAEPF
    private String cno; // 12 N - CNO
    private String razaoOuNome; // 1-150 A - Razão social or nome
    private LocalDate dataInicialAej; // 10 D - Data inicial
    private LocalDate dataFinalAej; // 10 D - Data final
    private LocalDateTime dataHoraGerAej; // 24 DH - Data e hora geração
    private String versaoAej; // 3 A - "001"
}
