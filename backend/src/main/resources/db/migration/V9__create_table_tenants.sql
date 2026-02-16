Create table if not exists tenants  (
id BIGSERIAL primary key,
tenant_id  varchar(50) not null unique,
nome varchar(200) not null,
cnpj varchar(18) not null unique,
dominio varchar(100),
ativo BOOLEAN not null default false,
created_at timestamp,
updated_at timestamp
);


