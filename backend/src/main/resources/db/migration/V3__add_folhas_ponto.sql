-- Migration V3: Adiciona tabela folhas_ponto para Sprint 7 - Folha de Ponto Detalhada
-- Conforme especificado no SDD (Software Design Document)

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