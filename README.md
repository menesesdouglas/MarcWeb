# MarcWeb — Sistema de Ponto Eletrônico Web

Sistema REP-P (Registro Eletrônico de Ponto por Programa) multi-tenant desenvolvido com Spring Boot e frontend vanilla JS, conforme **Portaria 671/2021** do Ministério do Trabalho e Emprego.

---

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 17, Spring Boot 3.5, Spring Security, Hibernate |
| Banco de dados | PostgreSQL + Flyway (migrations) |
| Auth | JWT (JJWT 0.12) |
| Multi-tenancy | Schema-per-tenant (Hibernate MultiTenancy) |
| PDF | iText 8 |
| Frontend | HTML/CSS/JS vanilla |

---

## Arquitetura

```
MarcWeb
├── backend/          # Spring Boot API
│   └── src/main/java/com/marcweb/
│       ├── controller/     # REST endpoints
│       ├── service/        # Regras de negócio
│       ├── entity/         # Entidades JPA
│       ├── repository/     # Spring Data JPA
│       ├── security/       # JWT + Spring Security
│       ├── hibernate/      # Multi-tenancy (schema-per-tenant)
│       ├── dto/            # DTOs de entrada e saída
│       ├── exceptions/     # Exceções customizadas
│       └── auditoria/      # Aspecto de auditoria (@Auditado)
└── frontend/         # HTML/CSS/JS estático
```

### Multi-tenancy

Cada empresa recebe um **schema PostgreSQL isolado** (`tenant_<tenantId>`). O tenant é resolvido via claim `tenant_id` do JWT ou header `X-Tenant-Id`. O `TenantContext` (ThreadLocal) carrega o schema em cada requisição.

---

## Pré-requisitos

- Java 17+
- PostgreSQL 14+
- Maven 3.9+
- Node.js (opcional, apenas para ferramentas de build)

---

## Configuração

### 1. Variáveis de ambiente (`.env` na raiz de `backend/`)

```env
# Banco principal
DB_URL_DEV=jdbc:postgresql://localhost:5432/marcweb
POSTGRES_USER=seu_usuario
POSTGRES_PASSWORD=sua_senha

# JWT — gere com: openssl rand -base64 48
JWT_SECRET=<chave_base64>

# (Opcional) rotação de chaves
APPLICATION_SECURITY_JWT_KID=key-2026-01
APPLICATION_SECURITY_JWT_PREVIOUS_KEYS=
```

### 2. Banco de dados

```sql
CREATE DATABASE marcweb;
```

O Flyway aplica as migrations automaticamente na inicialização.

### 3. Executar

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

O servidor sobe em `http://localhost:8080`. O frontend está em `frontend/` e pode ser servido por qualquer servidor estático.

---

## Endpoints principais

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| POST | `/api/auth/login` | Público | Login, retorna JWT |
| GET | `/api/tenant` | Público | Lista empresas ativas |
| POST | `/api/tenant/create` | Admin-key header | Cria novo tenant |
| POST | `/api/ponto` | EMPLOYEE | Registra ponto |
| GET | `/api/ponto/me` | EMPLOYEE | Histórico de pontos |
| GET | `/api/ponto/status-atual` | ADMIN | Status de todos os funcionários |
| POST | `/api/ponto/ajuste` | ADMIN | Ajuste manual de ponto |
| POST | `/api/folha-ponto` | ADMIN | Gera folha de ponto (JSON) |
| POST | `/api/relatorios/pdf` | ADMIN | Gera folha em PDF (base64) |
| GET | `/api/ponto/{id}/pdf` | EMPLOYEE | Comprovante individual PDF |
| POST | `/api/folgas` | ADMIN | Cadastra folga |
| POST | `/api/folgas/solicitar` | EMPLOYEE | Solicita folga |
| PUT | `/api/folgas/solucionar/{id}/{aprovada}` | ADMIN | Aprova/recusa folga |
| POST | `/api/feriado/sync` | ADMIN | Sincroniza feriados nacionais (BrasilAPI) |
| GET | `/api/auditoria/logs` | ADMIN | Logs de auditoria com filtros |

---

## Regras de negócio

### Registro de ponto
- Sequência obrigatória: `ENTRADA → INICIO_DESCANSO → FIM_DESCANSO → SAÍDA`
  - Saída direta após entrada é permitida (sem intervalo)
- Bloqueios automáticos: domingo, feriados cadastrados, usuário em folga aprovada
- Novo ciclo bloqueado se a última saída foi há menos de 8 horas
- Geolocalização opcional por empresa (raio configurável em metros)
- Anti-duplicate: registros simultâneos são eliminados via transação

### Folha de ponto
- Calculada a partir dos registros agrupados por `expediente_id` (UUID único por turno)
- Horas extras: acima de 8h/dia ou média > 44h/semana
- Horas débito: abaixo de 8h/dia + dias de falta × 8h
- Período precisa estar fechado (mês anterior ou mais antigo)

### Planos e limites de funcionários
| Plano | Limite |
|-------|--------|
| FREEMIUM | 3 ativos |
| BASICO | 15 ativos |
| PREMIUM | 29 ativos |
| ENTERPRISE | Ilimitado |

### Auditoria
Toda ação relevante é registrada via AOP (`@Auditado`) na tabela `logs_auditoria`, incluindo sucesso e erro.

---

## Migrations Flyway

| Versão | Descrição |
|--------|-----------|
| V1 | Schema base |
| V2–V7 | Campos adicionais (tipo folga, folha ponto, cargo, CPF, status) |
| V8 | `expediente_id` em registros_ponto |
| V9 | Tabela `tenants` (schema público) |
| V10–V11 | `tenant_id` em users + constraint unique |
| V12–V13 | `precisao_maxima` e `plano` em configs_empresa |
| V14–V15 | `ativacoes` em users |
| V16 | `plano` em tenants |

---

## Criação de tenant

```bash
curl -X POST http://localhost:8080/api/tenant/create \
  -H "X-Admin-Key: marcweb_admin_2026" \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"empresa1","nome":"Empresa Exemplo","cnpj":"00.000.000/0001-00","plano":"BASICO"}'
```

O schema `tenant_empresa1` é criado automaticamente com usuário `admin` / `admin123`.

---

## Testes

```bash
cd backend
./mvnw test -Dspring.profiles.active=test
```

Requer banco de testes configurado em `application-test.yml` via variáveis `DB_URL_TEST`, `POSTGRES_USER_TEST`, `POSTGRES_PASSWORD_TEST`.

---

## Conformidade legal

- **Portaria 671/2021** — comprovantes individuais de ponto com código de autenticação
- **AEJ** (Arquivo Eletrônico de Jornada) — geração e validação com assinatura digital RSA
- **AFD** (Arquivo Fonte de Dados) — estrutura suportada conforme anexo da portaria
- Logs imutáveis para segurança jurídica

---

## Licença

Proprietária — TechHub Soluções Tecnológicas.
