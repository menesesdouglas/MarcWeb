package com.marcweb.dto;

import com.marcweb.entity.TipoFolga;

import java.time.LocalDate;

public record FolgaResponse(
        Long id,
        Long userId,
        String userName,
        LocalDate dataInicio,
        LocalDate dataFim,
        TipoFolga tipo,
        String justificativa,
        boolean aprovada
) {

    public FolgaResponse(com.marcweb.entity.Folga folga) {
        this(
                folga.getId(),
                folga.getUser().getId(),
                folga.getUser().getNome(),
                folga.getDataInicio(),
                folga.getDataFim(),
                folga.getTipo(),
                folga.getJustificativa(),
                folga.isAprovada()
        );
    }
}