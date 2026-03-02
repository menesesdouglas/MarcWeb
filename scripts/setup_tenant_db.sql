-- Script SQL para criar schema e tabelas de um novo tenant
-- Este script será executado para cada novo tenant criado

-- Função para criar schema e tabelas de um tenant
CREATE OR REPLACE FUNCTION criar_schema_tenant(tenant_id_param VARCHAR) RETURNS VOID AS $$
DECLARE
    schema_name VARCHAR := 'tenant_' || tenant_id_param;
BEGIN
    -- Cria o schema
    EXECUTE 'CREATE SCHEMA ' || quote_ident(schema_name);
    
    -- Cria as tabelas no schema do tenant
    EXECUTE 'CREATE TABLE ' || quote_ident(schema_name) || '.users (
        id BIGSERIAL PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        nome VARCHAR(150) NOT NULL,
        username VARCHAR(100) NOT NULL,
        password VARCHAR(255) NOT NULL,
        role VARCHAR(20) NOT NULL,
        ativo BOOLEAN DEFAULT TRUE,
        consent_geoloc BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        cargo VARCHAR(100),
        cpf VARCHAR(20),
        ativacoes INTEGER DEFAULT 1 NOT NULL
    )';
    
    -- Cria índice único para tenant + username
    EXECUTE 'CREATE UNIQUE INDEX idx_users_tenant_username ON ' || quote_ident(schema_name) || '.users(tenant_id, username)';
    
    -- Cria índice para tenant
    EXECUTE 'CREATE INDEX idx_users_tenant_id ON ' || quote_ident(schema_name) || '.users(tenant_id)';
    
    -- Tabela de configuração da empresa
    EXECUTE 'CREATE TABLE ' || quote_ident(schema_name) || '.configs_empresa (
        id BIGSERIAL PRIMARY KEY,
        nome_empresa VARCHAR(200) NOT NULL,
        lat_centro DOUBLE PRECISION,
        long_centro DOUBLE PRECISION,
        raio_geoloc INTEGER DEFAULT 100,
        geoloc_ativa BOOLEAN DEFAULT FALSE,
        precisao_maxima INTEGER DEFAULT 500
    )';
    
    -- Tabela de registros de ponto
    EXECUTE 'CREATE TABLE ' || quote_ident(schema_name) || '.registros_ponto (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        tipo VARCHAR(30) NOT NULL,
        timestamp TIMESTAMP NOT NULL,
        latitude DOUBLE PRECISION,
        longitude DOUBLE PRECISION,
        ip VARCHAR(45),
        precisao DOUBLE PRECISION,
        justificativa TEXT,
        expediente_id UUID
    )';
    
    -- Cria índice para registros_ponto
    EXECUTE 'CREATE INDEX idx_registro_user_data ON ' || quote_ident(schema_name) || '.registros_ponto(user_id, timestamp)';
    
    -- Tabela de feriados
    EXECUTE 'CREATE TABLE ' || quote_ident(schema_name) || '.feriados (
        id BIGSERIAL PRIMARY KEY,
        data DATE NOT NULL,
        nome VARCHAR(150) NOT NULL,
        origem VARCHAR(20) NOT NULL
    )';
    
    -- Cria índice para feriados
    EXECUTE 'CREATE INDEX idx_feriados_data ON ' || quote_ident(schema_name) || '.feriados(data)';
    
    -- Tabela de folgas
    EXECUTE 'CREATE TABLE ' || quote_ident(schema_name) || '.folgas (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        data_inicio DATE NOT NULL,
        data_fim DATE NOT NULL,
        tipo VARCHAR(20) DEFAULT ''FOLGA'',
        status VARCHAR(20) DEFAULT ''Pendente'',
        justificativa TEXT,
        dias_calculados INTEGER DEFAULT 0,
        aprovada BOOLEAN
    )';
    
    -- Tabela de folhas de ponto
    EXECUTE 'CREATE TABLE ' || quote_ident(schema_name) || '.folhas_ponto (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        mes INTEGER NOT NULL,
        ano INTEGER NOT NULL,
        total_horas_trabalhadas VARCHAR(10),
        total_horas_extras VARCHAR(10),
        total_horas_faltantes VARCHAR(10),
        status VARCHAR(20) DEFAULT ''Pendente'',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, mes, ano)
    )';
    
    -- Tabela de logs de auditoria
    EXECUTE 'CREATE TABLE ' || quote_ident(schema_name) || '.logs_auditoria (
        id BIGSERIAL PRIMARY KEY,
        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        user_id BIGINT,
        acao VARCHAR(150) NOT NULL,
        categoria VARCHAR(30) NOT NULL,
        detalhes TEXT,
        status VARCHAR(20) NOT NULL
    )';
    
    -- Cria índice para logs de auditoria
    EXECUTE 'CREATE INDEX idx_logs_user_data ON ' || quote_ident(schema_name) || '.logs_auditoria(user_id, timestamp)';
    
    -- Insere configuração padrão da empresa
    EXECUTE 'INSERT INTO ' || quote_ident(schema_name) || '.configs_empresa (nome_empresa, raio_geoloc, geoloc_ativa) 
             VALUES (' || quote_literal('Empresa ' || tenant_id_param) || ', 100, false)';
    
    -- Cria usuário admin padrão
    EXECUTE 'INSERT INTO ' || quote_ident(schema_name) || '.users (nome, username, password, role, ativo, consent_geoloc, created_at, tenant_id, cargo, cpf, ativacoes) 
             VALUES (' || quote_literal('Administrador') || ', ''admin'', ''$2a$10$DlyZMkqoQ7yqQZqQZqQZqOuTqQZqQZqQZqQZqQZqQZqQZqQZqQZqQZqQZq'', ''ADMIN'', true, false, CURRENT_TIMESTAMP, ' || quote_literal(tenant_id_param) || ', ''Administrador'', ''000.000.000-00'', 1)';
END;
$$ LANGUAGE plpgsql;

-- Função para remover schema de um tenant (para testes)
CREATE OR REPLACE FUNCTION remover_schema_tenant(tenant_id_param VARCHAR) RETURNS VOID AS $$
DECLARE
    schema_name VARCHAR := 'tenant_' || tenant_id_param;
BEGIN
    EXECUTE 'DROP SCHEMA IF EXISTS ' || quote_ident(schema_name) || ' CASCADE';
END;
$$ LANGUAGE plpgsql;
