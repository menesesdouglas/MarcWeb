-- Adiciona coluna CPF na tabela users
ALTER TABLE users ADD COLUMN cpf VARCHAR(14) NOT NULL  DEFAULT '';

-- Remove o valor padrão após garantir que não há registros sem CPF
UPDATE users SET cpf = '' WHERE cpf IS NULL;

-- Cria índice único para CPF (opcional, caso queira garantir CPFs únicos)
-- ALTER TABLE users ADD CONSTRAINT uk_users_cpf UNIQUE (cpf);