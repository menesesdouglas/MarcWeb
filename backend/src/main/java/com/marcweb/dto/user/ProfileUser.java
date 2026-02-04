package com.marcweb.dto.user;

import com.marcweb.dto.folga.FolgaPendenteDetalhada;
import lombok.Data;

import java.util.List;

@Data
public class ProfileUser {

    private final String nome;
    private final String cargo;

    /// UTILIZANDO FOLGAS PENDENTES PARA APROVEITAR A CLASSE
    private List<FolgaPendenteDetalhada> folgas;

    /// PARA IMPROVISAR PAGINAÇÃO
    private long totalPagesFolgas;


}
