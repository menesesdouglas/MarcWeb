package com.marcweb.dto.afd;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AFDSensitiveEventsDTO {
    private String nsr; // 001-009 9 N NSR
    private String tipoReg; // 010-010 1 N "6"
    private LocalDateTime dataHoraGravacao; // 011-034 24 DH Data e hora gravação
    private String tipoEvento; // 035-036 2 N Tipo evento (01-08)
}
