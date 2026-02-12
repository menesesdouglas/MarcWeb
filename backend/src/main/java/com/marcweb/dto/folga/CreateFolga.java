package com.marcweb.dto.folga;

import com.marcweb.entity.TipoFolga;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateFolga(
        @NotNull(message = "O ID do usuário é obrigatório.")
        Long userId, // ID do usuário associado à folga

        @NotNull(message = "A data de início é obrigatória.")
        LocalDate dataInicio, // Data de início da folga

        @NotNull(message = "A data de fim é obrigatória.")
        LocalDate dataFim, // Data de término da folga

        @NotNull(message = "O tipo de folga é obrigatório.")
        TipoFolga tipo, // Tipo da folga (FERIAS, ATESTADO, FALTA_JUSTIFICADA)

        @NotBlank(message = "A justificativa é obrigatória.")
        @Size(max = 500, message = "A justificativa não pode ter mais de 500 caracteres.")
        String justificativa // Justificativa para a folga
) {
    // Validação personalizada: dataInicio deve ser menor ou igual a dataFim
    public CreateFolga {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data de início deve ser anterior ou igual à data de fim.");
        }
    }
}