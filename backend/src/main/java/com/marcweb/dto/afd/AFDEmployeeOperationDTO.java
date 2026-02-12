package com.marcweb.dto.afd;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AFDEmployeeOperationDTO {
    private String nsr; // 001-009 9 N NSR
    private String tipoReg; // 010-010 1 N "5"
    private LocalDateTime dataHoraGravacao; // 011-034 24 DH Data e hora gravação
    private String tipoOperacao; // 035-035 1 A "I", "A", "E"
    private String cpfEmpregado; // 036-047 12 N CPF empregado
    private String nomeEmpregado; // 048-099 52 A Nome empregado
    private String demaisDadosIdentificacao; // 100-103 4 A Demais dados identificação
    private String cpfResponsavel; // 104-114 11 N CPF responsável
    private String crc16; // 115-118 4 A CRC-16
}
