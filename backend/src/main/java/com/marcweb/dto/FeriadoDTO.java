package com.marcweb.dto;

import java.time.LocalDate;

public record FeriadoDTO(
        LocalDate date,
        String name,
        String type
) {
}
