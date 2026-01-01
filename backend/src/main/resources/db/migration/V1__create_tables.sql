-- USERS
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    consent_geoloc BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CONFIG EMPRESA (recurso único)
CREATE TABLE configs_empresa (
    id BIGSERIAL PRIMARY KEY,
    nome_empresa VARCHAR(200) NOT NULL,
    lat_centro DOUBLE PRECISION,
    long_centro DOUBLE PRECISION,
    raio_geoloc INTEGER DEFAULT 100,
    geoloc_ativa BOOLEAN DEFAULT FALSE
);

-- REGISTROS DE PONTO
CREATE TABLE registros_ponto (
    id BIGSERIAL PRIMARY KEY,
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
    justificativa TEXT,

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
