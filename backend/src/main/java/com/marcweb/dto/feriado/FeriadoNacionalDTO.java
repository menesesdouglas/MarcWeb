package com.marcweb.dto.feriado;

import java.time.LocalDate;

public record FeriadoNacionalDTO(
        LocalDate date,
        String name,
        String type
) {
}
