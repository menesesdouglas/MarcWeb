package com.marcweb.hibernate;

import com.marcweb.service.TenantSchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/// CLASSE RESPONSÁVEL POR MUDAR O SCHEMA ATUAL
@Slf4j
@RequiredArgsConstructor
@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider {

    private final DataSource dataSource;
    private final TenantSchemaService schemaService;

    private static final String SCHEMA_PREFIX = "tenant_";

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }


    /// MÉTODO QUE CONECTA COM O SCHEMA
    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        
        try {
            String tenantId = tenantIdentifier.toString();
            
            // Para tenants especiais (public, legacy), usar o schema public diretamente
            // sem criar um novo schema
            if ("public".equals(tenantId) || "legacy".equals(tenantId)) {
                log.debug("Usando schema public para tenant especial: {}", tenantId);
                connection.createStatement().execute("set search_path to public");
            } else {
                // Para tenants normais, usar verificação leve com cache
                String fullSchemaName = SCHEMA_PREFIX + tenantId;
                
                // Verificação leve - usa cache para evitar queries repetidas
                boolean schemaReady = schemaService.isSchemaReady(tenantId);
                
                if (!schemaReady) {
                    // Se não está pronto, faz a configuração completa (apenas na primeira vez)
                    try {
                        schemaService.ensureTenantSchema(tenantId);
                    } catch (Exception e) {
                        log.error("Erro ao configurar schema para tenant {}: {}", tenantId, e.getMessage());
                        // Não vamos falhar completamente, vamos tentar continuar
                    }
                }
                
                connection.createStatement().execute("set search_path to " + fullSchemaName);
            }
        } catch (SQLException e) {
            throw new HibernateException("Não foi possível alterar para o schema " + tenantIdentifier, e);
        }
        return connection;
    }

    /**
     * Verifica se um schema existe no banco de dados
     */
    private boolean schemaExists(Connection connection, String schemaName) throws SQLException {
        String sql = "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schemaName + "'";
        try (Statement stmt = connection.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        }
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        // Restaurar para schema público antes de fechar
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("set search_path to public");
        } catch (SQLException e) {
            log.warn("Erro ao restaurar search_path: {}", e.getMessage());
        }
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class aClass) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return null;
    }
}
