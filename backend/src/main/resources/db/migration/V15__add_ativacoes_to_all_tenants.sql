-- Migration V15: Adiciona coluna ativacoes em todos os tenants existentes
-- Executa para cada schema de tenant (exclui 'public' que é o schema do Flyway)

DO $$
DECLARE
    tenant_schema TEXT;
    table_exists BOOLEAN;
BEGIN
    -- Loop por todos os schemas que começam com 'tenant_'
    FOR tenant_schema IN 
        SELECT schema_name 
        FROM information_schema.schemata 
        WHERE schema_name LIKE 'tenant_%'
    LOOP
        -- Verificar se a tabela users existe neste schema
        SELECT EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_schema = tenant_schema 
            AND table_name = 'users'
        ) INTO table_exists;
        
        -- Se a tabela existir, adiciona a coluna
        IF table_exists THEN
            -- Adiciona a coluna se não existir
            EXECUTE format('ALTER TABLE %I.users ADD COLUMN IF NOT EXISTS ativacoes int', tenant_schema);
            
            -- Atualiza valores existentes para 1
            EXECUTE format('UPDATE %I.users SET ativacoes = 1 WHERE ativacoes IS NULL', tenant_schema);
            
            -- Define valor padrão e NOT NULL
            EXECUTE format('ALTER TABLE %I.users ALTER COLUMN ativacoes SET DEFAULT 1', tenant_schema);
            EXECUTE format('ALTER TABLE %I.users ALTER COLUMN ativacoes SET NOT NULL', tenant_schema);
            
            -- Log da operação
            RAISE NOTICE 'Coluna ativacoes adicionada/atualizada no schema: %', tenant_schema;
        ELSE
            RAISE NOTICE 'Tabela users nao encontrada no schema: %', tenant_schema;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Migration V15 concluida para todos os tenants';
END $$;
