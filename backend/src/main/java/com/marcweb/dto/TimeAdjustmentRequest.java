package com.marcweb.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimeAdjustmentRequest(
    Long userId,
    LocalDate date,
    String tipo,
    LocalTime time,
    String justificativa
) {
}