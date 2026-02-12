package com.marcweb.dto.afd;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AFDPointMarkingREP_PDTO {
    private String nsr; // 001-009 9 N NSR
    private String tipoReg; // 010-010 1 A "7"
    private LocalDateTime dataHoraMarcacao; // 011-034 24 DH Data e hora marcação
    private String cpfEmpregado; // 035-046 12 N CPF empregado
    private LocalDateTime dataHoraGravacao; // 047-070 24 DH Data e hora gravação
    private String identificadorColetor; // 071-072 2 N Identificador coletor (01-05)
    private Integer marcacaoOnlineOffline; // 073-073 1 N 0: online, 1: offline
    private String codigoHash; // 074-137 64 A Código hash SHA-256
}
