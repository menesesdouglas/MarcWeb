package com.marcweb.dto.afd;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AFDPointMarkingREP_C_ADTO {
    private String nsr; // 001-009 9 N NSR
    private String tipoReg; // 010-010 1 A "3"
    private LocalDateTime dataHoraMarcacao; // 011-034 24 DH Data e hora marcação
    private String cpfEmpregado; // 035-046 12 N CPF empregado
    private String crc16; // 047-050 4 A CRC-16
}
