package com.marcweb.dto.afd;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AFDHeaderDTO {
    private String nsr; // 001-009 9 N "000000000"
    private String tipoReg; // 010-010 1 N "1"
    private Integer tpIdtEmpregador; // 011-011 1 N 1: CNPJ, 2: CPF
    private String idtEmpregador; // 012-025 14 N CNPJ or CPF
    private String cnoOuCaepf; // 026-039 14 N CNO or CAEPF
    private String razaoSocialOuNome; // 040-189 150 A Razão social or nome
    private String numeroFabricacaoOuProcessoOuRegistro; // 190-206 17 N Número fabricação/Processo/Registro
    private LocalDate dataInicial; // 207-216 10 D Data inicial
    private LocalDate dataFinal; // 217-226 10 D Data final
    private LocalDateTime dataHoraGeracao; // 227-250 24 DH Data e hora geração
    private String versaoLeiaute; // 251-253 3 N "003"
    private Integer tpIdtFabricante; // 254-254 1 N 1: CNPJ, 2: CPF
    private String idtFabricante; // 255-268 14 N CNPJ or CPF fabricante
    private String modelo; // 269-298 30 A Modelo (REP-C)
    private String crc16; // 299-302 4 A CRC-16
}
