package com.marcweb.hibernate;

/// CONTEXTO DO TENANT DA REQUISIÇÃO
public class TenantContext {

    ///  SCHEMA PADRÃO
    private static final String DEFAULT_SCHEMA = "public";

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
