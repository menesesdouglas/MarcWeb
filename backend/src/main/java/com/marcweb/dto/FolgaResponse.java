package com.marcweb.dto;

import com.marcweb.entity.TipoFolga;
import java.time.LocalDate;

public record FolgaResponse(
        Long id,
        Long userId,
        String userName, // Nome do usuário associado
        LocalDate dataInicio,
        LocalDate dataFim,
        TipoFolga tipo,
        String justificativa
) {
    // Construtor que recebe uma entidade Folga e um nome de usuário
    public FolgaResponse(com.marcweb.entity.Folga folga) {
        this(
            folga.getId(),
            folga.getUser().getId(),
            folga.getUser().getNome(), // Supondo que a entidade User tenha um getter getNome()
            folga.getDataInicio(),
            folga.getDataFim(),
            folga.getTipo(),
            folga.getJustificativa()
        );
    }
}