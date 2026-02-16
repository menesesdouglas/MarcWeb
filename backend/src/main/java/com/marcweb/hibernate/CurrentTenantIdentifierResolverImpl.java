package com.marcweb.hibernate;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    /// REPASSANDO O TENANT ENCONTRADO NO HEADER ´PARA O IDENTIFICADOR DO HIBERNATE
    @Override
    public Object resolveCurrentTenantIdentifier() {
        return TenantContext.getCurrentTenant();
    }

    /// GARANTE QUE O HIBERNATE VALIDE A SESSÃO CASO O TENANT SEJA MUDADO
    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
