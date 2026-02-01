package com.marcweb.dto.folga;

import com.marcweb.entity.TipoFolga;

import java.time.LocalDate;

public record FolgaPendente(
        Long id,
        String nomeFuncionario,
        LocalDate dataInicio,
        LocalDate dataFim,
        String tipo
) {
    public FolgaPendente(com.marcweb.entity.Folga folga) {
        this(
                folga.getId(),
                folga.getUser().getNome(),
                folga.getDataInicio(),
                folga.getDataFim(),
                folga.getTipo().getText()
        );
    }
}
