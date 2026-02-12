package com.marcweb.dto.aej;

import lombok.Data;

@Data
public class AEJHorarioContratualDTO {
    private String tipoReg; // 2 N - "04"
    private String codHorContratual; // 1-30 A - Código do horário contratual
    private Integer durJornada; // 1-12 N - Duração da jornada em minutos
    private String hrEntrada01; // 4 H - Hora da primeira entrada (hhmm)
    private String hrSaida01; // 4 H - Hora da primeira saída (hhmm)
    private String hrEntrada02; // 0 or 4 H - Hora da segunda entrada (hhmm)
    private String hrSaida02; // 0 or 4 H - Hora da segunda saída (hhmm)
}
