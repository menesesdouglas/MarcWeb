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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/// CLASSE RESPONSÁVEL POR MUDAR O SCHEMA DA REQUISIÇÃO
@Slf4j
@RequiredArgsConstructor
@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider {

    private final DataSource dataSource;
    private final TenantSchemaService schemaService;

    private static final String SCHEMA_PREFIX = "tenant_";
    
    // Cache para marcar tenants que já foram verificados quanto às colunas
    // Isso evita verificações repetidas e vazamento de conexões
    private final Set<String> columnsVerifiedTenants = ConcurrentHashMap.newKeySet();

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
                // Para tenants normais
                String fullSchemaName = SCHEMA_PREFIX + tenantId;

                // Verificação leve - usa cache para evitar queries repetidas
                boolean schemaReady = schemaService.isSchemaReady(tenantId);

                if (!schemaReady) {
                    // Se não está pronto, faz a configuração completa (apenas na primeira vez)
                    try {
                        schemaService.ensureTenantSchema(tenantId);
                        // Após ensureTenantSchema, marca como verificado
                        columnsVerifiedTenants.add(tenantId);
                    } catch (Exception e) {
                        log.error("Erro ao configurar schema para tenant {}: {}", tenantId, e.getMessage());
                        // Não vamos falhar completamente, vamos tentar continuar
                    }
                } else if (!columnsVerifiedTenants.contains(tenantId)) {
                    // Schema existe mas ainda não verificamos as colunas
                    // Usa a conexão atual para verificar/adicionar colunas
                    try {
                        addMissingColumnsWithConnection(connection, tenantId);
                        columnsVerifiedTenants.add(tenantId);
                        log.debug("Colunas verificadas/adicionadas para tenant {}", tenantId);
                    } catch (Exception e) {
                        log.warn("Erro ao verificar colunas para tenant {}: {}", tenantId, e.getMessage());
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
     * Adiciona colunas faltantes usando a conexão fornecida
     * Este método NÃO abre uma nova conexão, usa a existente
     */
    private void addMissingColumnsWithConnection(Connection connection, String tenantId) throws SQLException {
        String schemaName = SCHEMA_PREFIX + tenantId;
        
        // Primeiro, muda para o schema do tenant
        connection.setSchema(schemaName);
        
        try (Statement stmt = connection.createStatement()) {
            // Migration V3 - Adicionar colunas da tabela folhas_ponto
            try {
                stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS dados TEXT");
            } catch (SQLException e) {
                // Coluna pode já existir
            }

            try {
                stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS horas_trabalhadas DECIMAL(10,2)");
            } catch (SQLException e) {
                // Coluna pode já existir
            }

            try {
                stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS horas_extras DECIMAL(10,2) DEFAULT 0");
            } catch (SQLException e) {
                // Coluna pode já existir
            }

            try {
                stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS horas_debito DECIMAL(10,2) DEFAULT 0");
            } catch (SQLException e) {
                // Coluna pode já existir
            }

            try {
                stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS dias_trabalhados INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Coluna pode já existir
            }

            try {
                stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS dias_falta INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Coluna pode já existir
            }

            try {
                stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS gerada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            } catch (SQLException e) {
                // Coluna pode já existir
            }
            
            log.debug("Colunas da tabela folhas_ponto verificadas para tenant {}", tenantId);
        }
        // Não precisa restaurar o schema aqui, pois o releaseConnection já faz isso
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
    
    /**
     * Limpa o cache de verificação de colunas (útil para testes ou forçar re-verificação)
     */
    public void clearColumnsCache() {
        columnsVerifiedTenants.clear();
        log.info("Cache de colunas verificado limpo");
    }
}
