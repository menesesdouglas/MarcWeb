package com.marcweb.dto;

import java.time.LocalDate;

public record FeriadoNacionalDTO(
        LocalDate date,
        String name,
        String type
) {
}
