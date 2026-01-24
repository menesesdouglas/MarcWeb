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
- [x] Criar entidade `FolhaPonto` com dados em JSONB.
- [x] Migration V3 para tabela folhas_ponto.
- [ ] Implementar FolhaPontoRepository.
- [ ] Implementar FolhaPontoService com motor de cálculo.
- [ ] Criar FolhaPontoController com endpoints REST.
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

### **Fase 4: Conformidade Portaria 671/2021 (Sprints 17-18)**

* **Sprint 17:** Implementação de AEJ e Comprovantes
  - Geração de Arquivo Eletrônico de Jornada (AEJ)
  - Comprovantes de ponto em PDF com assinatura PAdES
  - Integração com ICP-Brasil para assinaturas digitais

* **Sprint 18:** Armazenamento de Registro de Ponto (ARP)
  - Implementação de ARP com redundância
  - Mecanismo de imutabilidade de registros
  - Auditoria completa de todas as operações

---

### **Fase 5: Preparação para Produção (Sprints 19-20)**

* **Sprint 19:** Configuração de Servidor Cloud
  - Setup de servidor Hetzner Cloud
  - Configuração de firewall e segurança
  - Monitoramento e alertas

* **Sprint 20:** Deploy em Produção
  - Dockerização completa
  - Backup e recuperação de desastres
  - Documentação de produção

---

## 🛠️ Tecnologias
* **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA.
* **Banco de Dados:** PostgreSQL 15+, Flyway (Migrações).
* **Frontend:** Pico CSS (Minimalista/Leve).
* **API Externa:** BrasilAPI (Feriados e CEP).

## 🔐 Segurança e Configuração

### Geração de Chave JWT
Para gerar uma chave segura para JWT, utilize o comando:
```bash
openssl rand -base64 48
```
Copie a saída e configure no arquivo `.env` como `JWT_SECRET`.

### Rotação de Chaves JWT
Para rotacionar chaves JWT sem invalidar tokens existentes:

1. **Prepare a nova chave**:
   ```bash
   openssl rand -base64 48
   ```
   Anote o valor gerado.

2. **Atualize o arquivo `.env`**:
   ```env
   JWT_SECRET=<nova_chave_base64>
   APPLICATION_SECURITY_JWT_KID=key-2026-02
   APPLICATION_SECURITY_JWT_PREVIOUS_KEYS=key-2026-01:<chave_antiga_base64>
   ```

3. **Distribua a nova chave** para todos os nós da aplicação.

4. **Aguarde o período de transição** (até que todos os tokens antigos expirem).

5. **Remova a chave antiga** do `previous-keys` quando não houver mais tokens com o `kid` antigo.

### Variáveis de Ambiente
O projeto utiliza variáveis de ambiente para configurações sensíveis. Copie `.env.example` para `.env` e preencha com valores seguros:

```bash
cp backend/.env.example backend/.env
```

**Importante:** Nunca comite o arquivo `.env` com valores reais. Ele está listado no `.gitignore`.

### Configuração de Ambientes
- **Desenvolvimento**: `application-dev.yml`
- **Produção**: `application-prod.yml`

Para ativar o perfil de produção:
```bash
java -jar marcweb.jar --spring.profiles.active=prod
```

### Forçar HTTPS
Em produção, o HTTPS é ativado por padrão. Para desenvolvimento, você pode ativar manualmente:
```env
SSL_ENABLED=true
SSL_KEYSTORE_PASSWORD=<sua_senha>
```


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
