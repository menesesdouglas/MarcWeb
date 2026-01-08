# 📋 Relatório Completo do Projeto MarcWeb

**Última Atualização:** 08/01/2026  
**Versão:** 0.0.1-dev  
**Status:** Sprint 6 em andamento

---

## 📊 Resumo Executivo

| Métrica | Valor | Status |
|---------|-------|--------|
| Sprints Completas | 6/16 | 37.5% |
| Bugs Críticos | 0 | 🔴 |
| Bugs Corrigidos | 9/9 | ✅ |
| Cobertura Testes | ~40% | 🟡 |
| Funcionalidades MVP | 50% | 🟡 |

---

## 🏗️ Visão Geral do Projeto

### Descrição
**MarcWeb** é um sistema de registro de ponto eletrônico com funcionalidades de gestão de funcionários, controle de frequência, folha de ponto automatizada e relatórios. O sistema foi projetado para ser single-tenant inicialmente (MVP) e posteriormente evoluir para arquitetura multi-tenant.

### Stack Tecnológico
- **Backend:** Java 17 + Spring Boot 3.x
- **Banco de Dados:** PostgreSQL 14+ com Flyway migrations
- **Segurança:** JWT (JSON Web Tokens)
- **Frontend:** HTML5, CSS3, Vanilla JavaScript
- **Build Tool:** Maven
- **Testes:** JUnit 5, Mockito

---

## ⚠️ Problemas Arquiteturais

### PA-001: Testes Automatizados Insuficientes
**Status:** ⚠️ PARCIALMENTE IMPLEMENTADO  
**Problema:** Cobertura de testes ainda baixa (<70%)

### PA-002: LogAuditoria Criado Mas Não Usado
**Status:** ⏸️ NÃO INICIADO  
**Problema:** Entidade existe mas nenhum serviço grava logs

### PA-003: Sem Validação Sobreposição Folgas
**Status:** ⚠️ PARCIALMENTE IMPLEMENTADO  
**Problema:** Sistema pode aceitar folgas conflitantes

### PA-004: Sem Paginação em Listagens
**Status:** ⏸️ NÃO INICIADO  
**Problema:** `GET /api/ponto/me` pode retornar milhares de registros

### PA-005: Falta FolhaPonto Entity
**Status:** ⏸️ NÃO INICIADO (Sprint 7)  
**Problema:** Sprint 7 referencia entidade inexistente

---

## ✅ Funcionalidades Implementadas

### Autenticação e Segurança
- [x] Endpoint `/api/auth/login`
- [x] Endpoint `/api/auth/register`
- [x] JWT token generation e validação
- [x] `@PreAuthorize` para controle de acesso
- [x] Roles: ADMIN e EMPLOYEE

### Gestão de Usuários (Admin)
- [x] CRUD completo de usuários
- [x] Validações de entrada
- [x] Endpoint `GET /api/user/{id}`
- [x] Endpoint `PUT /api/user/{id}`
- [x] Endpoint `DELETE /api/user/{id}`

### Registro de Ponto
- [x] Endpoint `POST /api/registro-ponto/registrar`
- [x] Validação de sequência (ENTRADA → INICIO_DESCANSO → FIM_DESCANSO → SAIDA)
- [x] Validação de geolocalização (Haversine)
- [x] Bloqueio aos domingos
- [x] Bloqueio em feriados
- [x] Endpoint `GET /api/ponto/me`
- [x] Endpoint `GET /api/ponto/detalhes/{id}`

### Feriados
- [x] CRUD de feriados manuais
- [x] Sincronização com BrasilAPI
- [x] Endpoint `POST /api/feriado/nacionais`
- [x] Endpoint `GET /api/feriado?startDate=X&endDate=Y`
- [x] Endpoint `DELETE /api/feriado/{id}`

### Folgas
- [x] CRUD de folgas
- [x] Tipos: FERIAS, ATESTADO, FALTA_JUSTIFICADA
- [x] Validação de datas
- [x] Domingos automáticos

### Configuração da Empresa
- [x] Entity `ConfigEmpresa`
- [x] Endpoint `GET /api/config-empresa`
- [x] Endpoint `PUT /api/config-empresa`
- [x] Geolocalização configurável

### Utilities
- [x] Health check: `/api/health`
- [x] BrasilApiService para feriados nacionais
- [x] Cálculo de distância Haversine

---

## ❌ Funcionalidades Não Implementadas (MVP)

| Funcionalidade | Sprint | Status |
|----------------|--------|--------|
| Folha de Ponto JSON | 7 | ⏸️ NÃO INICIADO |
| Logs de Auditoria | 8 | ⏸️ NÃO INICIADO |
| Biblioteca Comum (common-lib) | 9-10 | ⏸️ NÃO INICIADO |
| Relatórios PDF | 11 | ⏸️ NÃO INICIADO |
| Testes Unitários Completos | 12 | ⚠️ PARCIAL |
| Multi-tenant | 13-16 | ⏸️ NÃO INICIADO |
| Contracheque | - | 📋 REQUISITO NOVO |

---

## 📋 Requisitos Funcionais

### Registro de Ponto
- **RF-001:** Funcionário registra ponto via API
- **RF-002:** Validação de sequência de registros
- **RF-003:** Bloqueio de registro em domingos
- **RF-004:** Bloqueio de registro em feriados
- **RF-005:** Geolocalização opcional (configurável por empresa)
- **RF-006:** Validação de raio de atuação

### Gestão de Feriados
- **RF-007:** Cadastro manual de feriados
- **RF-008:** Sincronização automática com BrasilAPI
- **RF-009:** Consulta de feriados por período

### Gestão de Folgas
- **RF-010:** Cadastro de folgas por funcionário
- **RF-011:** Tipos de folga (Férias, Atestado, Falta Justificada)
- **RF-012:** Validação de sobreposição de folgas
- **RF-013:** Domingos automáticos como folga

### Contracheque (NOVO)
- **RF-022:** Cálculo de horas trabalhadas do mês
- **RF-023:** Configurável por horista ou salário base
- **RF-024:** Cálculo de desconto INSS
- **RF-025:** Cálculo de FG

---

## 🔧 Endpoints da API

### Autenticação
| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/api/auth/login` | Login com JWT | Público |
| POST | `/api/auth/register` | Cadastrar usuário | ADMIN |

### Usuários
| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| GET | `/api/user/{id}` | Buscar usuário | ADMIN |
| GET | `/api/user` | Listar usuários | ADMIN |
| PUT | `/api/user/{id}` | Editar usuário | ADMIN |
| DELETE | `/api/user/{id}` | Excluir usuário | ADMIN |

### Registro de Ponto
| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/api/registro-ponto/registrar` | Registrar ponto | EMPLOYEE |
| GET | `/api/ponto/me` | Meus registros | EMPLOYEE |
| GET | `/api/ponto/detalhes/{id}` | Detalhes registro | EMPLOYEE |

### Feriados
| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/api/feriado` | Cadastrar feriado | ADMIN |
| POST | `/api/feriado/nacionais` | Sincronizar nacionais | ADMIN |
| GET | `/api/feriado` | Listar por período | EMPLOYEE |
| DELETE | `/api/feriado/{id}` | Excluir feriado | ADMIN |

### Folgas
| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/api/folgas` | Cadastrar folga | EMPLOYEE |
| GET | `/api/folgas` | Minhas folgas | EMPLOYEE |
| PUT | `/api/folgas/{id}` | Editar folga | EMPLOYEE |
| DELETE | `/api/folgas/{id}` | Excluir folga | EMPLOYEE |

### Configuração
| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| GET | `/api/config-empresa` | Ver configuração | EMPLOYEE |
| PUT | `/api/config-empresa` | Atualizar configuração | ADMIN |

### Saúde
| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| GET | `/api/health` | Health check | Público |

---

## 📈 Progresso das Sprints

### Fase 1: Fundação (Sprints 1-4) ✅ COMPLETO
| Sprint | Status | Entregas |
|--------|--------|----------|
| Sprint 1 | ✅ | Setup, PostgreSQL, Spring Boot inicial, health check |
| Sprint 2 | ✅ | 6 entidades JPA, JWT, login |
| Sprint 3 | ✅ | CRUD usuários, validações, Flyway |
| Sprint 4 | ✅ | UserService, register, BrasilApiService |

### Fase 2: Core do Negócio (Sprints 5-12)
| Sprint | Status | Entregas |
|--------|--------|----------|
| Sprint 5 | ✅ | RegistroPonto completo, geolocalização, validações |
| Sprint 6 | ✅ | Feriados sincronizados, CRUD folgas |
| Sprint 7 | ⏸️ NÃO INICIADO | Folha de Ponto JSON |
| Sprint 8 | ⏸️ NÃO INICIADO | Logs de Auditoria |
| Sprint 9-10 | ⏸️ NÃO INICIADO | Biblioteca Comum (common-lib) |
| Sprint 11 | ⏸️ NÃO INICIADO | Relatórios PDF |
| Sprint 12 | ⚠️ PARCIAL | Testes Unitários |

### Fase 3: Multi-tenant (Sprints 13-16)
| Sprint | Status | Entregas |
|--------|--------|----------|
| Sprint 13 | ⏸️ NÃO INICIADO | Eureka + Gateway |
| Sprint 14 | ⏸️ NÃO INICIADO | Feign Clients |
| Sprint 15 | ⏸️ NÃO INICIADO | Multi-empresa |
| Sprint 16 | ⏸️ NÃO INICIADO | Deploy e Documentação |

---

## 🎯 Estimativa de Conclusão

| Fase | Estimativa | Status |
|------|------------|--------|
| MVP (Sprint 12) | ~44 dias úteis | 37.5% completo |
| Multi-tenant (Sprint 16) | ~90 dias úteis | 37.5% completo |

---

## ⚡ Ações Imediatas Recomendadas

### Prioridade 1 (Esta Semana)
1. ✅ Corrigir BUG-010 em FeriadoRepository
2. Criar testes para RegistroPontoService
3. Remover senha hardcoded do application-dev.yml

### Prioridade 2 (Próxima Sprint)
4. Implementar Sprint 7 - Folha de Ponto
5. Criar endpoint GET /api/user
6. Adicionar paginação em listagens

### Prioridade 3 (Médio Prazo)
7. Implementar Logs de Auditoria
8. Adicionar Handler genérico de erros
9. Documentação OpenAPI/Swagger

---

## 📝 Notas Importantes

### SSL e Deploy
- Para ambiente de produção, configurar SSL no Nginx (não na aplicação)
- Nginx encapsula a comunicação HTTPS

### Contracheque (Requisito Novo)
- Cálculo de horas trabalhadas do mês
- Configurável: horista ou salário base
- Descontos: INSS, VT, VR, Plano de Saúde
- Adicionais: Abonos, horas extras, adicional noturno

---

**FIM DO DOCUMENTO**
- **RF-018:** Saldo de horas (extras - débito)
