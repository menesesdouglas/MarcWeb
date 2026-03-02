package com.marcweb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class TenantSchemaService {

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;

    private static final String SCHEMA_PREFIX = "tenant_";
    
    // Cache em memória para schemas já verificados/configurados
    // Evita verificações repetidas a cada requisição
    private final Set<String> initializedTenants = ConcurrentHashMap.newKeySet();
    // Cache para verificação leve de existência de schema
    private final Set<String> existingSchemas = ConcurrentHashMap.newKeySet();

    /**
     * Garante que o schema e as tabelas existam para o tenant
     * Este método usa cache para evitar verificações repetidas
     * @param tenantId O identificador único do tenant
     */
    public void ensureTenantSchema(String tenantId) {
        // Verificação rápida com cache para evitar processamento desnecessário
        // Mas ainda assim verifica/adiciona colunas faltantes para garantir compatibilidade
        boolean alreadyInitialized = initializedTenants.contains(tenantId);
        
        if (alreadyInitialized) {
            // Mesmo que o tenant já esteja inicializado, ainda adiciona colunas faltantes
            // Isso garante que novas colunas adicionadas ao código sejam adicionadas aos tenants existentes
            addMissingColumnsIfNeeded(tenantId);
            log.debug("Tenant {} já inicializado, mas verificando colunas faltantes", tenantId);
            return;
        }
        
        synchronized (this) {
            // Verificação dupla dentro do synchronized
            if (initializedTenants.contains(tenantId)) {
                addMissingColumnsIfNeeded(tenantId);
                return;
            }
            
            String schemaName = SCHEMA_PREFIX + tenantId;
            
            try (Connection connection = dataSource.getConnection()) {
                // Verificar se schema existe
                boolean schemaExists = schemaExists(connection, schemaName);
                
                if (!schemaExists) {
                    // Criar schema se não existir
                    createSchema(connection, schemaName);
                    log.info("Schema {} criado com sucesso", schemaName);
                } else {
                    existingSchemas.add(tenantId);
                }
                
                // Criar tabelas apenas se for a primeira vez ou schema foi criado
                createTablesInSchema(connection, schemaName, tenantId);
                
                // Criar admin apenas se for necessário
                createDefaultAdminUser(connection, schemaName, tenantId);
                
                // Marcar como inicializado
                initializedTenants.add(tenantId);
                
                log.info("Tenant {} configurado com sucesso", tenantId);
            } catch (SQLException e) {
                log.error("Erro ao configurar schema para tenant {}: {}", tenantId, e.getMessage());
                throw new HibernateException("Não foi possível configurar o schema do tenant", e);
            }
        }
    }

    /**
     * Verificação leve apenas para mudar o schema de conexão
     * Usado pelo MultiTenantConnectionProvider para mudança rápida de schema
     * @param tenantId O identificador único do tenant
     * @return true se o schema existe
     */
    public boolean isSchemaReady(String tenantId) {
        // Retorna true se já foi inicializado
        if (initializedTenants.contains(tenantId)) {
            return true;
        }
        
        // Verificação rápida no banco
        String schemaName = SCHEMA_PREFIX + tenantId;
        if (existingSchemas.contains(tenantId)) {
            return true;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            boolean exists = schemaExists(connection, schemaName);
            if (exists) {
                existingSchemas.add(tenantId);
            }
            return exists;
        } catch (SQLException e) {
            log.error("Erro ao verificar schema: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cria o schema do tenant e todas as tabelas necessárias
     * @param tenantId O identificador único do tenant
     * @deprecated Use ensureTenantSchema em vez deste método
     */
    @Deprecated
    public void createTenantSchema(String tenantId) {
        ensureTenantSchema(tenantId);
    }

    /**
     * Verifica se um schema existe no banco de dados
     */
    private boolean schemaExists(Connection connection, String schemaName) throws SQLException {
        String sql = "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schemaName + "'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        }
    }

    /**
     * Cria um novo schema no banco de dados
     */
    private void createSchema(Connection connection, String schemaName) throws SQLException {
        String sql = "CREATE SCHEMA " + schemaName;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Cria todas as tabelas necessárias no schema do tenant
     */
    private void createTablesInSchema(Connection connection, String schemaName, String tenantId) throws SQLException {
        // Usar o schema atual
        connection.setSchema(schemaName);
        
        try (Statement stmt = connection.createStatement()) {
            // Tabela de usuários - CRÍTICO PARA LOGIN
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "nome VARCHAR(150) NOT NULL, " +
                "username VARCHAR(100) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL, " +
                "ativo BOOLEAN DEFAULT TRUE, " +
                "consent_geoloc BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "tenant_id VARCHAR(50) NOT NULL, " +
                "cargo VARCHAR(100), " +
                "cpf VARCHAR(20))"
            );

            // Criar índice único para tenant + username
            stmt.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_users_tenant_username ON users(tenant_id, username)"
            );

            // Criar índice para tenant
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id)");

            // Tabela de configuração da empresa (incluindo plano - V16)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS configs_empresa (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "nome_empresa VARCHAR(200) NOT NULL, " +
                "lat_centro DOUBLE PRECISION, " +
                "long_centro DOUBLE PRECISION, " +
                "raio_geoloc INTEGER DEFAULT 100, " +
                "geoloc_ativa BOOLEAN DEFAULT FALSE, " +
                "plano VARCHAR(30) DEFAULT 'BASICO')"
            );

            // Tabela de registros de ponto (incluindo expediente_id da migration V8)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS registros_ponto (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "tipo VARCHAR(30) NOT NULL, " +
                "timestamp TIMESTAMP NOT NULL, " +
                "latitude DOUBLE PRECISION, " +
                "longitude DOUBLE PRECISION, " +
                "ip VARCHAR(45), " +
                "precisao DOUBLE PRECISION, " +
                "justificativa TEXT, " +
                "expediente_id UUID)"
            );

            // Criar índice para registros_ponto
            stmt.execute(
                "CREATE INDEX IF NOT EXISTS idx_registro_user_data ON registros_ponto(user_id, timestamp)"
            );

            // Tabela de feriados
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS feriados (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "data DATE NOT NULL, " +
                "nome VARCHAR(150) NOT NULL, " +
                "origem VARCHAR(20) NOT NULL)"
            );

            // Criar índice para feriados
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_feriados_data ON feriados(data)");

            // Tabela de folgas (incluindo aprovada da migration V7)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS folgas (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "data_inicio DATE NOT NULL, " +
                "data_fim DATE NOT NULL, " +
                "tipo VARCHAR(20) DEFAULT 'FOLGA', " +
                "status VARCHAR(20) DEFAULT 'Pendente', " +
                "justificativa TEXT, " +
                "dias_calculados INTEGER DEFAULT 0, " +
                "aprovada BOOLEAN)"
            );

            // Tabela de folhas de ponto
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS folhas_ponto (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "mes INTEGER NOT NULL, " +
                "ano INTEGER NOT NULL, " +
                "total_horas_trabalhadas VARCHAR(10), " +
                "total_horas_extras VARCHAR(10), " +
                "total_horas_faltantes VARCHAR(10), " +
                "status VARCHAR(20) DEFAULT 'Pendente', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(user_id, mes, ano))"
            );

            // Tabela de logs de auditoria
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS logs_auditoria (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "user_id BIGINT, " +
                "acao VARCHAR(150) NOT NULL, " +
                "categoria VARCHAR(30) NOT NULL, " +
                "detalhes TEXT, " +
                "status VARCHAR(20) NOT NULL)"
            );

            // Criar índice para logs de auditoria
            stmt.execute(
                "CREATE INDEX IF NOT EXISTS idx_logs_user_data ON logs_auditoria(user_id, timestamp)"
            );

            // Inserir configuração padrão da empresa (incluindo plano - V16)
            String insertConfig = "INSERT INTO configs_empresa (nome_empresa, raio_geoloc, geoloc_ativa, plano) " +
                "SELECT 'Empresa " + tenantId + "', 100, false, 'BASICO' WHERE NOT EXISTS (SELECT 1 FROM configs_empresa)";
            stmt.execute(insertConfig);

            // Adicionar colunas faltantes em tabelas já existentes (para tenants existentes)
            addMissingColumns(stmt);

            log.debug("Tabelas verificadas/criadas com sucesso no schema {}", schemaName);
        } finally {
            // Restaurar para schema público
            connection.setSchema("public");
        }
    }

    /**
     * Adiciona colunas faltantes em tabelas já existentes
     * Isso garante que tenants antigos tenham as colunas adicionadas pelas migrations
     */
    private void addMissingColumns(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE folgas ADD COLUMN IF NOT EXISTS aprovada BOOLEAN");
        } catch (SQLException e) {
            log.debug("Coluna 'aprovada' pode já existir: {}", e.getMessage());
        }

        try {
            stmt.execute("ALTER TABLE registros_ponto ADD COLUMN IF NOT EXISTS expediente_id UUID");
        } catch (SQLException e) {
            log.debug("Coluna 'expediente_id' pode já existir: {}", e.getMessage());
        }

        try {
            stmt.execute("ALTER TABLE configs_empresa ADD COLUMN IF NOT EXISTS plano VARCHAR(30) DEFAULT 'BASICO'");
        } catch (SQLException e) {
            log.debug("Coluna 'plano' pode já existir: {}", e.getMessage());
        }

        // Migration V3 - Adicionar colunas da tabela folhas_ponto
        try {
            stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS dados TEXT");
        } catch (SQLException e) {
            log.debug("Coluna 'dados' pode já existir: {}", e.getMessage());
        }

        try {
            stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS horas_trabalhadas DECIMAL(10,2)");
        } catch (SQLException e) {
            log.debug("Coluna 'horas_trabalhadas' pode já existir: {}", e.getMessage());
        }

        try {
            stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS horas_extras DECIMAL(10,2) DEFAULT 0");
        } catch (SQLException e) {
            log.debug("Coluna 'horas_extras' pode já existir: {}", e.getMessage());
        }

        try {
            stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS horas_debito DECIMAL(10,2) DEFAULT 0");
        } catch (SQLException e) {
            log.debug("Coluna 'horas_debito' pode já existir: {}", e.getMessage());
        }

        try {
            stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS dias_trabalhados INTEGER DEFAULT 0");
        } catch (SQLException e) {
            log.debug("Coluna 'dias_trabalhados' pode já existir: {}", e.getMessage());
        }

        try {
            stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS dias_falta INTEGER DEFAULT 0");
        } catch (SQLException e) {
            log.debug("Coluna 'dias_falta' pode já existir: {}", e.getMessage());
        }

        try {
            stmt.execute("ALTER TABLE folhas_ponto ADD COLUMN IF NOT EXISTS gerada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        } catch (SQLException e) {
            log.debug("Coluna 'gerada_em' pode já existir: {}", e.getMessage());
        }
    }

    /**
     * Cria o usuário admin padrão para o tenant
     */
    private void createDefaultAdminUser(Connection connection, String schemaName, String tenantId) throws SQLException {
        connection.setSchema(schemaName);
        
        try {
            // Codificar a senha com BCrypt
            String encodedPassword = passwordEncoder.encode("admin123");
            
            // Inserir usuário admin padrão usando PreparedStatement
            String insertSql = "INSERT INTO users (nome, username, password, role, ativo, consent_geoloc, created_at, tenant_id, cargo, cpf) " +
                "SELECT 'Administrador', 'admin', ?, 'ADMIN', true, false, CURRENT_TIMESTAMP, ?, 'Administrador', '000.000.000-00' " +
                "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')";
            
            try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                pstmt.setString(1, encodedPassword);
                pstmt.setString(2, tenantId);
                pstmt.executeUpdate();
            }
            
            log.debug("Usuário admin padrão verificado/criado para tenant {}", tenantId);
        } finally {
            connection.setSchema("public");
        }
    }

    /**
     * Verifica se o schema existe e está configurado
     */
    public boolean schemaExists(String tenantId) {
        // Primeiro verifica o cache
        if (initializedTenants.contains(tenantId) || existingSchemas.contains(tenantId)) {
            return true;
        }
        
        String schemaName = SCHEMA_PREFIX + tenantId;
        try (Connection connection = dataSource.getConnection()) {
            boolean exists = schemaExists(connection, schemaName);
            if (exists) {
                existingSchemas.add(tenantId);
            }
            return exists;
        } catch (SQLException e) {
            log.error("Erro ao verificar existência do schema: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Limpa o cache de tenants (útil para testes)
     */
    public void clearCache() {
        initializedTenants.clear();
        existingSchemas.clear();
        log.info("Cache de tenants limpo");
    }

    /**
     * Adiciona colunas faltantes em um tenant específico
     * Usado para atualizar tenants existentes com novas colunas
     * @param tenantId O identificador único do tenant
     */
    public void addMissingColumnsIfNeeded(String tenantId) {
        String schemaName = SCHEMA_PREFIX + tenantId;
        
        try (Connection connection = dataSource.getConnection()) {
            connection.setSchema(schemaName);
            try (Statement stmt = connection.createStatement()) {
                addMissingColumns(stmt);
                log.debug("Colunas atualizadas para tenant {}", tenantId);
            }
        } catch (SQLException e) {
            log.debug("Erro ao adicionar colunas para tenant {}: {}", tenantId, e.getMessage());
        }
    }
}
