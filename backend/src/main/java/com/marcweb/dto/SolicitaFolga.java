package com.marcweb.dto;

import com.marcweb.entity.TipoFolga;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;


public record SolicitaFolga(@NotNull(message = "A data de início é obrigatória.")
                            @FutureOrPresent(message = "A data de início não pode ser no passado.")
                            LocalDate dataInicio, // Data de início da folga

                            @NotNull(message = "A data de fim é obrigatória.")
                            @FutureOrPresent(message = "A data de fim não pode ser no passado.")
                            LocalDate dataFim, // Data de término da folga

                            @NotNull(message = "O tipo de folga é obrigatório.")
                            TipoFolga tipo, // Tipo da folga (FERIAS, ATESTADO, FALTA_JUSTIFICADA)

                            @NotBlank(message = "A justificativa é obrigatória.")
                            @Size(max = 500, message = "A justificativa não pode ter mais de 500 caracteres.")
                            String justificativa // Justificativa para a folga
) {
    // Validação personalizada: dataInicio deve ser menor ou igual a dataFim
    public SolicitaFolga {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data de início deve ser anterior ou igual à data de fim.");
        }


    }
}