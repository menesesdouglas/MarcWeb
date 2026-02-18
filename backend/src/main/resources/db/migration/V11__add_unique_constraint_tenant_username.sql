-- Criar constraint unique para (tenant_id + username)
-- Isso permite que o mesmo username exista em tenants diferentes
ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_tenant_username;
ALTER TABLE users ADD CONSTRAINT uk_users_tenant_username UNIQUE (tenant_id, username);
