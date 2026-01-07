# marc-web 🕒

**Aplicação WEB de gestão de ponto eletrônico** Migração moderna do sistema Marc (original em Python) para web, com foco em segurança, conformidade CLT/LGPD e simplicidade para PMEs.

---

## 🚀 Status do Projeto
* **Fase atual:** Fase 2 — Core do Negócio.
* **Último Marco:** Sprint 6 concluída (07/01/2026).
* **Próximo Desafio:** Sprint 7 — Geração de Folha de Ponto.
* **Arquitetura:** Monolito Spring Boot (preparado para futura migração Multi-tenant).

---

## 🗺️ Plano SCRUM - MVP First

### **Fase 1: Fundação (Sprints 1-4) ✅ CONCLUÍDA**

* **Sprint 1: Setup Geral & Infra (30/12/2025)** * Ambientes funcionais (PC + Notebook), DB `marcweb_dev`, Endpoint `/api/health`.
* **Sprint 2: Entidades & Segurança (31/12/2025)** * Modelagem JPA, JWT implementado, Endpoint de Login e Usuário Admin padrão.
* **Sprint 3: CRUD Básico (01/01/2026)** * Controllers REST, CRUD de usuários, Flyway configurado.
* **Sprint 4: Validações Iniciais (02/01/2026)** * Lógica de negócio no Service, integração BrasilAPI e ajuste de estrutura de pastas.

---

### **Fase 2: Core do Negócio (Sprints 5-12) 🏗️ EM ANDAMENTO**

#### **Sprint 5: Registro de Ponto Completo ✅ (05/01/2026)**
- [x] RegistroPontoService com validação de sequência lógica.
- [x] Validação de geolocalização (Distância Haversine).
- [x] Bloqueio de registros em domingos e feriados.
- [x] Tratamento de exceções customizadas e `GlobalExceptionHandler`.
- [x] Correção BUG-001 (NullPointerException).

#### **Sprint 6: Calendário e Folgas ✅ (07/01/2026)**
- [x] Sincronização automática de feriados via API externa.
- [x] CRUD de Folgas (Férias, Atestados, Faltas Justificadas).
- [x] Automação de domingos como folga padrão.
- [x] Migration V2 para novos campos de auditoria e tipos de folga.

#### **Sprint 7: Folha de Ponto Detalhada ⭐ PRIORIDADE ALTA**
- [ ] Criar entidade `FolhaPonto` com dados em JSONB.
- [ ] Implementar motor de cálculo: Horas Trabalhadas vs. Horas Extras vs. Débitos.
- [ ] Criar endpoints de consulta para colaborador (`/me`) e gestor.
- [ ] Implementar lógica de fechamento mensal.

#### **Sprint 8: Sistema de Logs Unificado**
- [ ] Implementar Aspecto `@Auditado` para rastreio automático de ações.
- [ ] Endpoints de consulta de auditoria para administradores.

#### **Sprints 9-10: Preparação para Multi-tenant**
- [ ] Criação da `empresa-common-lib`.
- [ ] Refatoração para Interfaces de Serviço e Entidades Base.

#### **Sprints 11-12: Relatórios e Hardening**
- [ ] Exportação de relatórios em PDF (iText 7).
- [ ] Cobertura de testes unitários > 70%.

---

### **Fase 3: Multi-tenant (Sprints 13-16) 🚀 PLANEJAMENTO**

* **Sprint 13:** Setup de Service Discovery (Eureka) e API Gateway.
* **Sprint 14:** Comunicação entre micro-módulos via Feign Clients.
* **Sprint 15:** Implementação de isolamento físico de bancos por empresa.
* **Sprint 16:** Dockerização completa, Monitoramento e Deploy.

---

## 🛠️ Tecnologias
* **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA.
* **Banco de Dados:** PostgreSQL 15+, Flyway (Migrações).
* **Frontend:** Pico CSS (Minimalista/Leve).
* **API Externa:** BrasilAPI (Feriados e CEP).

---

## 📈 Histórico de Commits & Marcos

| Data | Marco | Descrição |
| :--- | :--- | :--- |
| 30/12/2025 | **Sprint 1** | Projeto inicializado e saúde do sistema validada. |
| 31/12/2025 | **Sprint 2** | Autenticação JWT e base de segurança funcional. |
| 05/01/2026 | **Sprint 5** | Sistema de ponto validado com geolocalização. |
| 07/01/2026 | **Sprint 6** | Gestão de feriados e folgas finalizada. |

---

**Próxima Atualização:** Final da Sprint 7.  
**Responsável:** Equipe de Desenvolvimento Marc-Web.
