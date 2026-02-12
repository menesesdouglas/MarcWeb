package com.marcweb.dto.afd;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AFDClockAdjustmentDTO {
    private String nsr; // 001-009 9 N NSR
    private String tipoReg; // 010-010 1 N "4"
    private LocalDateTime dataHoraAntesAjuste; // 011-034 24 DH Data e hora antes ajuste
    private LocalDateTime dataHoraAjustada; // 035-058 24 DH Data e hora ajustada
    private String cpfResponsavel; // 059-069 11 N CPF responsável
    private String crc16; // 070-073 4 A CRC-16
}
