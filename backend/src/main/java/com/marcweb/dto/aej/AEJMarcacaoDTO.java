package com.marcweb.dto.aej;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AEJMarcacaoDTO {
    private String tipoReg; // 2 N - "05"
    private String idtVinculoAej; // 1-9 N - Identificador do vínculo
    private LocalDateTime dataHoraMarc; // 24 DH - Data e hora da marcação
    private String idRepAej; // 0-9 N - Identificador do REP
    private String tpMarc; // 1 A - Tipo da marcação: E, S, D
    private Integer seqEntSaida; // 3 N - Número sequencial do par entrada/saída
    private String fonteMarc; // 1 A - Fonte da marcação: O, I, P, X, T
    private String codHorContratual; // 0-30 A - Código do horário contratual
    private String motivo; // 0-150 A - Motivo da desconsideração or inclusão
}
