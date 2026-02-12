package com.marcweb.dto.afd;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AFDCompanyIdentificationDTO {
    private String nsr; // 001-009 9 N NSR
    private String tipoReg; // 010-010 1 N "2"
    private LocalDateTime dataHoraGravacao; // 011-034 24 DH Data e hora gravação
    private String cpfResponsavel; // 035-048 14 N CPF responsável
    private Integer tpIdtEmpregador; // 049-049 1 N 1: CNPJ, 2: CPF
    private String idtEmpregador; // 050-063 14 N CNPJ or CPF
    private String cnoOuCaepf; // 064-077 14 N CNO or CAEPF
    private String razaoSocialOuNome; // 078-227 150 A Razão social or nome
    private String localPrestacaoServicos; // 228-327 100 A Local prestação serviços
    private String crc16; // 328-331 4 A CRC-16
}
