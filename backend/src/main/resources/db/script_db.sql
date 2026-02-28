-- USERS
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL DEFAULT 'legacy',
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    cargo VARCHAR(100),
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    ativacoes IN NOT NULL DEFAULT 1,
    consent_geoloc BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_tenant_username;
ALTER TABLE users ADD CONSTRAINT uk_users_tenant_username UNIQUE (tenant_id, username);

-- CONFIG EMPRESA (recurso único)
CREATE TABLE configs_empresa (
    id BIGSERIAL PRIMARY KEY,
    nome_empresa VARCHAR(200) NOT NULL,
    lat_centro DOUBLE PRECISION,
    long_centro DOUBLE PRECISION,
    raio_geoloc INTEGER DEFAULT 100,
    geoloc_ativa BOOLEAN DEFAULT FALSE,
    plano varchar(30) NOT NULL DEFAUL 'BASICO'
    CONSTRAINT configs_empresa_plano_check
    CHECK (plano IN ('FREEMIUM', 'BASICO', 'PREMIUM', 'ENTERPRISE'));
);
COMMENT ON CONSTRAINT configs_empresa_plano_check ON configs_empresa
IS 'Constraint que valida as planos de configs empresa conforme o enum de planos';

-- REGISTROS DE PONTO
CREATE TABLE registros_ponto (
    id BIGSERIAL PRIMARY KEY,
    expediente_id UUID not null,
    user_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    ip VARCHAR(45),
    precisao DOUBLE PRECISION,
    justificativa TEXT,

    CONSTRAINT fk_registro_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_registro_user_data
    ON registros_ponto (user_id, timestamp);

-- FERIADOS
CREATE TABLE feriados (
    id BIGSERIAL PRIMARY KEY,
    data DATE NOT NULL,
    nome VARCHAR(150) NOT NULL,
    origem VARCHAR(20) NOT NULL
);

CREATE INDEX idx_feriados_data
    ON feriados (data);

-- FOLGAS
CREATE TABLE folgas (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    justificativa TEXT,
    aprovada BOOLEAN,

    CONSTRAINT fk_folga_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- LOGS DE AUDITORIA
CREATE TABLE logs_auditoria (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT,
    acao VARCHAR(150) NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    detalhes TEXT,
    status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_log_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_logs_user_data
    ON logs_auditoria (user_id, timestamp);

ALTER TABLE logs_auditoria DROP CONSTRAINT IF EXISTS logs_auditoria_categoria_check;

-- Cria a nova constraint com todas as categorias do enum CategoriaLog
ALTER TABLE logs_auditoria
ADD CONSTRAINT logs_auditoria_categoria_check
CHECK (categoria IN ('PONTO', 'ADMIN', 'AUDITORIA', 'SISTEMA', 'RELATORIO', 'USO_SISTEMA'));

-- Comentário explicativo
COMMENT ON CONSTRAINT logs_auditoria_categoria_check ON logs_auditoria
IS 'Constraint que valida as categorias de log de auditoria conforme enum CategoriaLog';



 CREATE TABLE folhas_ponto (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES users(id),
        mes INTEGER NOT NULL CHECK (mes >= 1 AND mes <= 12),
        ano INTEGER NOT NULL CHECK (ano >= 2020 AND ano <= 2100),
        dados TEXT, -- JSON estruturado conforme SDD
        horas_trabalhadas DECIMAL(10,2),
        horas_extras DECIMAL(10,2) DEFAULT 0,
        horas_debito DECIMAL(10,2) DEFAULT 0,
        dias_trabalhados INTEGER DEFAULT 0,
        dias_falta INTEGER DEFAULT 0,
        gerada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, mes, ano)
    );

    -- Índice de performance para consultas por usuário e período
    CREATE INDEX idx_folhas_ponto_user_periodo
        ON folhas_ponto(user_id, ano, mes);

    -- Índice para consultas por período
    CREATE INDEX idx_folhas_ponto_periodo
        ON folhas_ponto(ano, mes);

    -- Comentários na tabela
    COMMENT ON TABLE folhas_ponto IS 'Folhas de ponto mensais detalhadas em formato JSON';
    COMMENT ON COLUMN folhas_ponto.dados IS 'Dados estruturados da folha em formato JSON conforme SDD';
    COMMENT ON COLUMN folhas_ponto.horas_trabalhadas IS 'Total de horas trabalhadas no mês (formato decimal: 176.50 = 176h 30min)';
    COMMENT ON COLUMN folhas_ponto.horas_extras IS 'Horas extras calculadas (acima de 8h/dia ou 220h/mês)';
    COMMENT ON COLUMN folhas_ponto.horas_debito IS 'Horas em débito (faltas ou horas não trabalhadas)';
    COMMENT ON COLUMN folhas_ponto.dias_trabalhados IS 'Quantidade de dias trabalhados no mês';
    COMMENT ON COLUMN folhas_ponto.dias_falta IS 'Quantidade de dias de falta no mês';
