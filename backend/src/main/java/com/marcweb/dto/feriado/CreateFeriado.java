package com.marcweb.dto.feriado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateFeriado {

    @NotNull(message = "Informe a data do feriado")
    private LocalDate data;

    @NotBlank(message = "Informe o nome do feriado")
    private String nome;
}
