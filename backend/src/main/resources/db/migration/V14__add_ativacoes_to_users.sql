ALTER TABLE users ADD COLUMN ativacoes int;

UPDATE users set ativacoes = 1 where id > 0;

ALTER TABLE users ALTER COLUMN ativacoes SET NOT NULL;

ALTER TABLE users ALTER COLUMN ativacoes SET DEFAULT 1;