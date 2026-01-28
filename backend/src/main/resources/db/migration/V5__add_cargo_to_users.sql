-- Adiciona coluna cargo à tabela users
ALTER TABLE users ADD COLUMN IF NOT EXISTS cargo VARCHAR(100);

-- Atualiza registros existentes para terem cargo padrão
UPDATE users SET cargo = 'Funcionário' WHERE cargo IS NULL;