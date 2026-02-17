package com.marcweb.auditoria;

import org.springframework.stereotype.Component;

/// CRIEI ESSA CLASSE PRA FICAR MAIS FÁCIL DE POR OS DETALHES NO LOG
@Component
public class Detalhes {
    private ThreadLocal<String> detalhes = ThreadLocal.withInitial(() -> "");

    public String getDetalhes() {
        return detalhes.get();
    }

    public void setDetalhes(String detalhes) {
        this.detalhes.set(detalhes);
    }
}
