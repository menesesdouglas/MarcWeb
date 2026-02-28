ALTER TABLE configs_empresa ADD COLUMN plano VARCHAR(30);

UPDATE configs_empresa SET plano = 'BASICO' where id > 0;

ALTER TABLE configs_empresa ALTER COLUMN plano SET NOT NULL;
ALTER TABLE configs_empresa ALTER COLUMN plano SET DEFAULT 'BASICO';

ALTER TABLE configs_empresa ADD CONSTRAINT
configs_empresa_plano_check CHECK (plano IN ('FREEMIUM', 'BASICO', 'PREMIUM', 'ENTERPRISE'));


COMMENT ON CONSTRAINT configs_empresa_plano_check ON configs_empresa
IS 'Constraint que valida as planos de configs empresa conforme o enum de planos';


