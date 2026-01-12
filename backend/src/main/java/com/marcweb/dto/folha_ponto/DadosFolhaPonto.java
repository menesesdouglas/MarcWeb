package com.marcweb.dto.folha_ponto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class DadosFolhaPonto {

    private UsuarioDTO usuario;
    private Periodo periodo;
    private List<Dia> dias;
    // TOTALIZAÇÃO DE HORAS DE HORAS
    private Totalizacao totalizacao;


    public void organizarDias() {
        dias.sort(Comparator.comparing(Dia::getData));
    }

}
