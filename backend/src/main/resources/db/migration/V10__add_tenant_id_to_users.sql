-- Adicionar coluna tenant_id na tabela users
-- Todos os usuários existentes serão associados ao tenant 'legacy'
ALTER TABLE users ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50) NOT NULL DEFAULT 'legacy';

-- Criar índice para melhorar performance em consultas por tenant
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);
