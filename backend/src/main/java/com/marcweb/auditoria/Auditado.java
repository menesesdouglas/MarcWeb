package com.marcweb.auditoria;

import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.StatusLog;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditado {

    String acao(); // Descrição da ação (ex: login, registro_ponto)

    CategoriaLog categoria(); // PONTO, ADMIN, AUDITORIA, etc.

    // Detalhes da ação
    String detalhes();
}
