package com.marcweb.dto.aej;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AEJAusenciaBancoHorasDTO {
    private String tipoReg; // 2 N - "07"
    private String idtVinculoAej; // 1-9 N - Identificador do vínculo
    private Integer tipoAusenOuComp; // 1 N - Código tipo ausência ou compensação: 1-4
    private LocalDate data; // 10 D - Data da ausência ou compensação
    private Integer qtMinutos; // 0-12 N - Quantidade de minutos
    private Integer tipoMovBH; // 0 or 1 N - Tipo de movimento no banco de horas: 1 or 2
}
