package com.marcweb.auditoria;

import lombok.Data;
import org.springframework.stereotype.Component;

/// CRIEI ESSA CLASSE PRA FICAR MAIS FÁCIL DE POR OS DETALHES NO LOG
@Data
@Component
public class Detalhes {
    private  String detalhes = "";
}
