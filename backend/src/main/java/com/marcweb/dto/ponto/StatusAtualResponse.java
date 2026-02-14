package com.marcweb.dto.ponto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusAtualResponse {
    private String status;
    private String ultimoRegistro;
    private boolean temRegistrosHoje;
}
