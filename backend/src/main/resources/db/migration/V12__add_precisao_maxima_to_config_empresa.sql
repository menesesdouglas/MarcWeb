-- Adiciona coluna precisao_maxima na tabela de configurações da empresa
ALTER TABLE configs_empresa 
ADD COLUMN IF NOT EXISTS precisao_maxima INTEGER DEFAULT 500;
