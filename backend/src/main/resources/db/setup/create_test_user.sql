-- Script para criar usuário de teste no PostgreSQL
-- Este script deve ser executado como superusuário (postgres)

-- Criar usuário de teste
CREATE USER marcuser_test WITH PASSWORD 'test123';

-- Conceder privilégios no banco de dados marcweb_test
GRANT ALL PRIVILEGES ON DATABASE marcweb_test TO marcuser_test;

-- Conectar ao banco de dados marcweb_test e conceder privilégios nas tabelas
\c marcweb_test

-- Conceder privilégios em todas as tabelas existentes
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO marcuser_test;

-- Conceder privilégios em todas as sequências
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO marcuser_test;

-- Conceder privilégios para criar novas tabelas
GRANT CREATE ON SCHEMA public TO marcuser_test;

-- Conceder privilégios para usar o schema
GRANT USAGE ON SCHEMA public TO marcuser_test;