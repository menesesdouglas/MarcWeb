-- Migration V16: Adiciona coluna plano na tabela tenants do schema public
-- O plano pode ser null pois será alterado manualmente posteriormente

ALTER TABLE tenants ADD COLUMN IF NOT EXISTS plano VARCHAR(30);

-- Opcional: Comentário para documentar a coluna
COMMENT ON COLUMN tenants.plano IS 'Plano do tenant: BASICO, PREMIUM, etc. Pode ser null para alteração manual';
