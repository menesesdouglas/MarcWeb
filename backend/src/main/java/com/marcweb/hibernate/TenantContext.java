package com.marcweb.hibernate;

/// CONTEXTO DO TENANT DA REQUISIÇÃO
public class TenantContext {

    ///  SCHEMA PADRÃO - legacy para dados existentes sem tenant específico
    private static final String DEFAULT_SCHEMA = "legacy";

    private static final ThreadLocal<String> currentTenant = ThreadLocal.withInitial(() -> DEFAULT_SCHEMA);


    public static void setCurrentTenant(String tenant) {
        currentTenant.set(tenant);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
