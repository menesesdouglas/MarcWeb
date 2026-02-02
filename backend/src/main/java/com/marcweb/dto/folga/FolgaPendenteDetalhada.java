package com.marcweb.dto.folga;

import com.marcweb.entity.TipoFolga;

import java.time.LocalDate;

public record FolgaPendenteDetalhada(
        Long id,
        String nomeFuncionario,
        LocalDate dataInicio,
        LocalDate dataFim,
        String tipo,
        String justificativa,
        Integer diasCalculados,
        LocalDate dataSolicitacao,
        String status
) {
    public FolgaPendenteDetalhada(com.marcweb.entity.Folga folga) {
        this(
                folga.getId(),
                folga.getUser().getNome(),
                folga.getDataInicio(),
                folga.getDataFim(),
                folga.getTipo().getText(),
                folga.getJustificativa(),
                calcularDias(folga.getDataInicio(), folga.getDataFim()),
                folga.getDataInicio(), // Considerando dataInicio como data da solicitação
                folga.getAprovada() == null ? "PENDENTE" : 
                (folga.getAprovada() ? "APROVADA" : "RECUSADA")
        );
    }

    private static Integer calcularDias(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }
}