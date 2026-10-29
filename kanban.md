# KANBAN — REP-P MVP

Registrador Eletrônico de Ponto via Programa
Conforme especificação em `README.md` (Portaria 671 do MTE)

---

## 1. Como usar este quadro

Cada Sprint (Semana) contém as tarefas organizadas em colunas:

| Coluna | Significado |
|--------|-------------|
| **Backlog** | Tarefa identificada, ainda não iniciada |
| **To Do** | Pronta para ser iniciada no sprint atual |
| **In Progress** | Em desenvolvimento |
| **Review / QA** | Em revisão, testes ou validação |
| **Done** | Concluída e aceita |
| **Blocked** | Bloqueada por dependência ou impedimento |

> **Duração total do MVP:** 4 semanas (1 sprint por semana), conforme a Seção 9 do README.

---

## 2. SPRINT 1 — Semana 1: Fundação e marcação básica

**Objetivo:** Colocar a infraestrutura no ar, autenticação funcionando e as duas marcações mínimas (entrada e saída).

### Backlog / To Do

| ID | Tarefa | Detalhe / Critério de aceitação | Responsável | Status |
|----|--------|----------------------------------|-------------|--------|
| S1.01 | Configurar repositório e versionamento | Estrutura de pastas frontend/backend, .gitignore, README atualizado | Dev | Done |
| S1.02 | Configurar execução local (sem Docker) | Backend com uvicorn, banco PostgreSQL local e frontend com Vite rodando no host | DevOps/Dev | Done |
| S1.03 | Configurar Alembic | Migrações de banco versionadas e aplicáveis na inicialização | Dev | Done |
| S1.04 | Implementar autenticação JWT | Login/logout, emissão de token, middleware de autenticação, hash de senha (bcrypt/Argon2) | Backend | Done |
| S1.05 | Cadastro de usuários (apenas ADM) | Criação de funcionário: nome, e-mail, CPF, matrícula, perfil. Validação de unicidade de e-mail e matrícula | Backend | Done |
| S1.06 | Telas de login e cadastro | Frontend: página de login e tela de cadastro de funcionário (restrita ao ADM) | Frontend | Done |
| S1.07 | Marcação de ponto: entrada e saída | Botões de entrada e saída com timestamp gerado exclusivamente pelo servidor | Backend + Frontend | Done |
| S1.08 | Extrato simples do funcionário | Lista das marcações do dia atual do próprio usuário | Backend + Frontend | Done |

### In Progress

| ID | Tarefa | Responsável | Início | Previsão |
|----|--------|-------------|--------|----------|

### Review / QA

| ID | Tarefa | Revisor | Status |
|----|--------|---------|--------|

### Done

| ID | Tarefa | Concluído em |
|----|--------|--------------|

### Blocked

| ID | Tarefa | Motivo do bloqueio | Desbloqueio previsto |
|----|--------|--------------------|----------------------|

---

## 3. SPRINT 2 — Semana 2: Jornada completa e ajustes

**Objetivo:** Implementar as quatro marcações padrão com validação de sequência e o fluxo completo de solicitação e aprovação de ajustes de ponto.

### Backlog / To Do

| ID | Tarefa | Detalhe / Critério de aceitação | Responsável | Status |
|----|--------|----------------------------------|-------------|--------|
| S2.01 | Implementar as quatro marcações | Entrada, saída intervalo, retorno intervalo, saída final, com validação de sequência | Backend | Done |
| S2.02 | Validação de sequência de marcações | Bloquear: saída sem entrada anterior no mesmo dia; duas entradas consecutivas sem saída; marcações com data futura; exclusão de registros | Backend | Done |
| S2.03 | Tela de solicitação de ajuste de ponto | Formulário com justificativa obrigatória, seleção do registro a ser ajustado ou indicação de nova marcação | Frontend | Done |

### In Progress

| ID | Tarefa | Responsável | Início | Previsão |
|----|--------|-------------|--------|----------|

### Review / QA

| ID | Tarefa | Revisor | Status |
|----|--------|---------|--------|

### Done

| ID | Tarefa | Concluído em |
|----|--------|--------------|
| S2.01 | Implementar as quatro marcações | 2026-10-29 |
| S2.02 | Validação de sequência de marcações | 2026-10-29 |
| S2.03 | Tela de solicitação de ajuste de ponto | 2026-10-29 |
| S2.04 | Fluxo de aprovação/rejeição pelo ADM | 2026-10-29 |
| S2.05 | Validações de negócio do ajuste | 2026-10-29 |
| S2.06 | Notificação visual de status do ajuste | 2026-10-29 |

### Blocked

| ID | Tarefa | Motivo do bloqueio | Desbloqueio previsto |
|----|--------|--------------------|----------------------|

---

## 4. SPRINT 3 — Semana 3: Dashboard ADM e relatórios

**Objetivo:** Entregar o painel de frequência do ADM, lista de ajustes pendentes e relatórios de frequência com exportação CSV.

### Backlog / To Do

| ID | Tarefa | Detalhe / Critério de aceitação | Responsável | Status |
|----|--------|----------------------------------|-------------|--------|
| S3.01 | Dashboard do ADM — ponto do dia | Lista de funcionários que não bateram ponto no dia (pendentes de entrada, saída ou intervalo) | Backend + Frontend | To Do |
| S3.02 | Dashboard do ADM — ajustes pendentes | Lista de ajustes pendentes de análise com ações rápidas | Backend + Frontend | To Do |
| S3.03 | Relatório de frequência mensal | Geração de relatório por funcionário com filtros por período, matrícula ou nome | Backend | To Do |
| S3.04 | Exportação CSV de relatórios | Colunas: data, tipo de marcação, horário registrado, ajustes aplicados e observações | Backend | To Do |
| S3.05 | Filtros de relatório por período e funcionário | Frontend com filtros de data inicial/final e seleção de funcionário | Frontend | To Do |
| S3.06 | Extrato individual com filtro por período | Consulta de marcações pessoais por data inicial e final (melhoria do S1.08) | Frontend | To Do |

### In Progress

| ID | Tarefa | Responsável | Início | Previsão |
|----|--------|-------------|--------|----------|

### Review / QA

| ID | Tarefa | Revisor | Status |
|----|--------|---------|--------|

### Done

| ID | Tarefa | Concluído em |
|----|--------|--------------|

### Blocked

| ID | Tarefa | Motivo do bloqueio | Desbloqueio previsto |
|----|--------|--------------------|----------------------|

---

## 5. SPRINT 4 — Semana 4: Auditoria, refinamento e deploy

**Objetivo:** Implementar logs de auditoria, revisar segurança/conformidade, documentar o uso, fazer deploy e validar com piloto.

### Backlog / To Do

| ID | Tarefa | Detalhe / Critério de aceitação | Responsável | Status |
|----|--------|----------------------------------|-------------|--------|
| S4.01 | Implementar logs de auditoria | Registro de ações críticas: quem fez, o quê, quando e em qual entidade (marcação, solicitação de ajuste, aprovação de ajuste, cadastro de usuário) | Backend | To Do |
| S4.02 | Consulta de logs de auditoria pelo ADM | Tela para o ADM investigar alterações no sistema | Backend + Frontend | To Do |
| S4.03 | Revisão de regras de segurança e conformidade | Senhas com hash criptográfico, HTTPS obrigatório em produção, JWT com expiração curta, proteção contra acesso a dados de outros usuários, validação de entrada em todos os endpoints | Backend | To Do |
| S4.04 | Documentação de uso | Guia para funcionário e para ADM (login, marcação, extrato, solicitação de ajuste, relatórios) | Produto/Tech Writer | To Do |
| S4.05 | Deploy em ambiente de nuvem | Deploy em Railway, Render ou VPS próprio, backend com Uvicorn atrás de proxy reverso (Nginx), PostgreSQL com volume persistente, HTTPS | DevOps | To Do |
| S4.06 | Validação final com piloto | Teste com poucos usuários reais, validação do fluxo end-to-end | QA + Produto | To Do |

### In Progress

| ID | Tarefa | Responsável | Início | Previsão |
|----|--------|-------------|--------|----------|

### Review / QA

| ID | Tarefa | Revisor | Status |
|----|--------|---------|--------|

### Done

| ID | Tarefa | Concluído em |
|----|--------|--------------|

### Blocked

| ID | Tarefa | Motivo do bloqueio | Desbloqueio previsto |
|----|--------|--------------------|----------------------|

---

## 6. BACKLOG FUTURO — Não incluído no MVP

Itens explicitamente excluídos da primeira versão (Seção 2.2 do README), para versões posteriores:

| ID | Tarefa | Motivo da exclusão | Versão alvo |
|----|--------|---------------------|-------------|
| FUT.01 | Vínculo com empresa/CNPJ | Arquitetura MVP é single-tenant | v2 |
| FUT.02 | Arquitetura multitenant (banco compartilhado) | Não está no escopo do MVP | v2+ |
| FUT.03 | White label automático | Cada empresa será deploy isolado no futuro | v2+ |
| FUT.04 | App mobile ou PWA | MVP é web (React SPA) | v2 |
| FUT.05 | Biometria, leitor de cartão ou reconhecimento facial | Não regulado no MVP | v2 |
| FUT.06 | Integração com eSocial | Fora do escopo do MVP | v2+ |
| FUT.07 | Banco de horas complexo (horas extras, saldo negativo, compensação) | Não incluído na primeira versão | v2 |
| FUT.08 | Assinatura digital ICP-Brasil em relatórios | Não exigida para o MVP | v2+ |

---

## 7. Tarefas transversais (infraestrutura e configuração)

Executadas ao longo de todos os sprints:

| ID | Tarefa | Detalhe | Sprint | Status |
|----|--------|---------|--------|--------|
| TR.01 | Variáveis de ambiente | URL do banco, chave secreta JWT, tempo de expiração, URL do frontend (CORS), nível de log, portas | S1 | Done |
| TR.02 | Índices de banco de dados | Índices em usuário, data da marcação e status do ajuste | S1 | Done |
| TR.03 | Middleware de log de requisições | Rastreabilidade de requisições na API | S1 | Done |
| TR.04 | Tratamento global de exceções | Respostas de erro padronizadas | S1 | Done |
| TR.05 | CORS configurado | Para o domínio do frontend | S1 | Done |
| TR.06 | Backups programados do banco | Recomendado para produção | S4 | To Do |
| TR.07 | HTTPS em produção | Certificado SSL/TLS obrigatório | S4 | To Do |

---

## 8. Checklist de definição de pronto (DoD)

Uma tarefa só entra na coluna **Done** quando:

- [ ] Código implementado e revisado (code review)
- [ ] Testes passando (unitários e/ou de integração)
- [ ] Migrações de banco aplicadas (quando houver alteração de esquema)
- [ ] Frontend e backend integrados e funcionando
- [ ] Conforme as regras arquiteturais do README (timestamp do servidor, imutabilidade de marcações, controle de perfil)
- [ ] Atualizado no kanban.md

---

**Gerado a partir de:** `README.md` — Seções 2 (Escopo), 7 (Funcionalidades), 9 (Roadmap de entrega), 10 (Frontend), 11 (Backend) e 12 (Banco de dados).
