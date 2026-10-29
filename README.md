# REP-P MVP — Registrador Eletrônico de Ponto via Programa

Aplicação web mínima viável para controle de jornada de trabalho, regulamentada pela **Portaria 671 do Ministério do Trabalho e Emprego (MTE)**.

---

## 1. Visão geral

O REP-P é um sistema de software que permite o registro eletrônico de marcações de ponto dos funcionários de uma empresa. Este repositório contém o **MVP (Minimum Viable Product)** da aplicação, focado nas funcionalidades essenciais para operação inicial com conformidade regulatória.

O objetivo do MVP é oferecer:

- Registro de marcações de ponto com timestamp confiável (servidor).
- Identificação do usuário por login e senha.
- Separação clara de perfis: funcionário e administrador (ADM).
- Fluxo de solicitação e aprovação de ajustes de ponto.
- Extrato de marcações para o funcionário.
- Dashboard e relatórios de frequência para o ADM.

---

## 2. Escopo do MVP

### 2.1 Incluído na primeira versão

| Módulo | Funcionalidades |
|--------|-----------------|
| Autenticação | Login e logout com senha. Controle de sessão por JWT. |
| Cadastro de usuários | Criação, edição e desativação de usuários (apenas ADM). Dois perfis: `funcionario` e `adm`. |
| Marcação de ponto | Registro das quatro marcações padrão (entrada, saída intervalo, retorno intervalo, saída) com validação de sequência. Timestamp gerado exclusivamente pelo servidor. |
| Extrato individual | Consulta das marcações do próprio usuário, por dia e por período. |
| Ajuste de ponto | Solicitação de correção com justificativa obrigatória. Fluxo: pendente → aprovado ou rejeitado pelo ADM. |
| Dashboard ADM | Visão dos funcionários que não bateram ponto no dia e lista de ajustes pendentes de análise. |
| Relatórios | Exportação de frequência mensal por funcionário em formato CSV. |
| Auditoria | Registro de ações críticas (quem fez, o quê, quando e em qual entidade). |

### 2.2 Não incluído no MVP (versões futuras)

- Vínculo com empresa/CNPJ.
- Arquitetura multitenant (banco compartilhado entre empresas).
- White label automático (cada empresa com banco separado será tratado como deploy isolado no futuro).
- App mobile ou PWA.
- Biometria, leitor de cartão ou reconhecimento facial.
- Integração com eSocial.
- Banco de horas complexo (horas extras, saldo negativo, compensação).
- Assinatura digital ICP-Brasil em relatórios.

---

## 3. Perfis de usuário

### 3.1 Funcionário

- Faz login com e-mail e senha.
- Marca ponto (entrada, saída intervalo, retorno, saída).
- Consulta o próprio extrato de marcações.
- Solicita ajustes de ponto com justificativa.
- Acompanha o status dos próprios ajustes (pendente, aprovado, rejeitado).

### 3.2 Administrador (ADM)

- Faz login com e-mail e senha.
- Cadastra, edita e desativa funcionários.
- Visualiza o dashboard de frequência do dia.
- Analisa e aprova ou rejeita ajustes de ponto solicitados.
- Exporta relatórios de frequência em CSV.
- Acessa logs de auditoria do sistema.

---

## 4. Stack tecnológica

| Camada | Tecnologia | Justificativa |
|--------|------------|---------------|
| Backend | FastAPI (Python) | API REST rápida de desenvolver, documentação automática (Swagger/OpenAPI), tipagem forte e boa integração com banco relacional. |
| Banco de dados | PostgreSQL | Robustez, confiabilidade, suporte a transações e integridade referencial. |
| Frontend | React (Vite) | Interface reativa para marcações e dashboards, ecossistema amplo e fácil manutenção. |
| Autenticação | JWT (JSON Web Token) | Stateless, adequado para API separada do frontend. |
| Hash de senha | bcrypt / Argon2 | Armazenamento seguro de credenciais. |
| Migrações | Alembic | Controle de versão do esquema do banco de dados. |
| Contêineres | Docker e Docker Compose | Padronização de ambiente de desenvolvimento e deploy. |

---

## 5. Arquitetura

A aplicação segue o modelo **cliente-servidor**, com separação clara entre frontend e backend.

```
+----------------+       HTTPS/JSON       +-------------------+
|   Frontend     | <====================> |     Backend       |
|  React (Vite)  |       API REST         |   FastAPI (Py)    |
+----------------+                        +-------------------+
                                              |
                                              | SQL
                                              v
                                        +-----------+
                                        | PostgreSQL|
                                        +-----------+
```

### Regras arquiteturais importantes

1. **Timestamp do servidor**: todas as marcações de ponto são gravadas com o horário do servidor backend. O frontend nunca envia ou confia no horário do navegador do usuário.
2. **Imutabilidade de marcações**: registros de ponto nunca são excluídos. Correções são tratadas por meio de ajustes vinculados, preservando o histórico original.
3. **Somente leitura para funcionários nos próprios dados**: o funcionário só pode consultar e solicitar alterações nas próprias marcações. Não pode visualizar dados de outros funcionários.
4. **Acesso restrito ao ADM**: cadastro de usuários, aprovação de ajustes e exportação de relatórios são exclusivos do perfil ADM.

---

## 6. Modelo de dados

### 6.1 Entidade: Usuario

Armazena os dados de acesso e perfil de cada pessoa no sistema.

- Identificador único.
- Nome completo.
- E-mail (único, usado para login).
- Senha (armazenada como hash, nunca em texto plano).
- CPF.
- Matrícula (única).
- Perfil: `funcionario` ou `adm`.
- Status: ativo ou inativo.
- Data de criação e atualização.

### 6.2 Entidade: RegistroPonto

Armazena cada marcação de ponto realizada.

- Identificador único.
- Usuário relacionado.
- Tipo de marcação: entrada, saída para intervalo, retorno do intervalo ou saída final.
- Timestamp gerado pelo servidor (data e hora oficiais).
- Endereço IP de origem da requisição.
- Origem do registro: web.
- Data de criação.

**Regras de validação de sequência:**
- Não é permitido registrar saída sem entrada anterior no mesmo dia.
- Não é permitido duas entradas consecutivas sem saída entre elas.
- Não é permitido registrar marcações com data futura.
- Não é permitido excluir registros de ponto.

### 6.3 Entidade: AjustePonto

Armazena solicitações de correção de ponto.

- Identificador único.
- Usuário solicitante.
- Registro de ponto relacionado (opcional, quando o ajuste se refere a uma marcação existente).
- Justificativa obrigatória (texto livre).
- Novo timestamp proposto para a correção.
- Status: pendente, aprovado ou rejeitado.
- Usuário ADM que analisou o ajuste.
- Data da análise (quando aprovado ou rejeitado).
- Data de criação da solicitação.

### 6.4 Entidade: AuditoriaLog (recomendado)

Registra ações críticas para rastreabilidade e compliance.

- Usuário que executou a ação.
- Tipo de ação (ex: marcação de ponto, solicitação de ajuste, aprovação de ajuste, cadastro de usuário).
- Entidade afetada.
- Identificador da entidade afetada.
- Detalhes adicionais (estrutura livre).
- Data e hora da ação.

---

## 7. Funcionalidades detalhadas

### 7.1 Autenticação e controle de acesso

- Tela de login com e-mail e senha.
- Validação de credenciais contra o banco de dados.
- Emissão de token JWT com validade configurável.
- Middleware de autenticação nas rotas que exigem login.
- Controle de autorização por perfil (`funcionario` ou `adm`).
- Logout com invalidação do token no lado do cliente.

### 7.2 Cadastro de usuários (ADM)

- Formulário para criar novo funcionário: nome, e-mail, CPF, matrícula e perfil.
- Validação de unicidade de e-mail e matrícula.
- Edição de dados cadastrais (exceto senha, que é alterada por fluxo separado se necessário).
- Desativação de usuários (soft delete ou bloqueio de acesso), sem exclusão física.

### 7.3 Marcação de ponto

- Interface com botões claros para as quatro marcações: entrada, saída intervalo, retorno intervalo, saída.
- Feedback visual imediato após a marcação (sucesso ou erro de validação).
- Exibição da última marcação realizada e do horário esperado para a próxima.
- Bloqueio de marcações fora da sequência válida.
- Exibição de aviso quando o funcionário tenta marcar ponto fora do horário de expediente configurado (se aplicável).

### 7.4 Extrato individual (funcionário)

- Lista cronológica das marcações do dia atual.
- Filtro por período (data inicial e final).
- Exibição do status de cada marcação e eventuais ajustes vinculados.
- Indicação visual de dias sem registro completo.

### 7.5 Ajuste de ponto

- Tela de solicitação com campo de justificativa obrigatório.
- Seleção do registro a ser ajustado ou indicação de nova marcação.
- Envio da solicitação para fila de aprovação.
- Tela do ADM com lista de ajustes pendentes.
- Ação de aprovar ou rejeitar, com opcional observação do ADM.
- Notificação visual ao funcionário quando o ajuste é aprovado ou rejeitado.

### 7.6 Dashboard do ADM

- Painel resumido do dia corrente.
- Lista de funcionários com ponto não batido (pendentes de entrada, saída ou intervalo).
- Lista de ajustes pendentes de análise.
- Acesso rápido aos relatórios de frequência.

### 7.7 Relatórios de frequência (ADM)

- Geração de relatório mensal por funcionário.
- Filtros por período, matrícula ou nome.
- Exportação em formato CSV.
- Inclusão de colunas: data, tipo de marcação, horário registrado, ajustes aplicados e observações.

### 7.8 Auditoria e compliance

- Log automático de ações sensíveis no sistema.
- Consulta de logs pelo ADM para investigação de alterações.
- Rastreamento de quem aprovou ou rejeitou cada ajuste e quando.

---

## 8. Requisitos não funcionais

### 8.1 Segurança

- Senhas armazenadas com hash criptográfico (bcrypt ou Argon2).
- Comunicação entre frontend e backend obrigatoriamente via HTTPS em produção.
- Tokens JWT com expiração curta e renovação controlada.
- Proteção contra acesso de funcionários a dados de outros usuários.
- Validação de entrada em todos os endpoints da API.

### 8.2 Integridade e conformidade (Portaria 671)

- Registro de ponto com hora oficial do servidor.
- Proibição de exclusão de marcações de ponto.
- Ajustes somente com justificativa e aprovação de perfil ADM.
- Rastreabilidade completa de alterações (logs de auditoria).

### 8.3 LGPD e privacidade

- Dados pessoais (CPF, e-mail, matrícula) acessíveis apenas a perfis autorizados.
- Minimização de coleta: apenas dados necessários para controle de ponto.
- Possibilidade de desativar usuários sem apagar histórico legalmente exigido.

### 8.4 Disponibilidade e desempenho

- Backend stateless, facilitando escalabilidade horizontal.
- Consultas de extrato indexadas por usuário e data.
- Frontend com carregamento rápido de páginas críticas (login e marcação).

---

## 9. Roadmap de entrega (4 semanas)

### Semana 1 — Fundação e marcacao basica [SPRINT 1 - CONCLUIDA]

- Configuracao do repositorio, Docker, Docker Compose, FastAPI, PostgreSQL e Alembic. (S1.01, S1.02, S1.03)
- Implementacao de autenticacao JWT e hash de senha. (S1.04)
- Cadastro de usuarios (apenas ADM cria funcionarios). (S1.05)
- Telas de login e cadastro. (S1.06)
- Marcacao de ponto: entrada e saida (minimo viavel). (S1.07)
- Extrato simples do funcionario (marcacoes do dia). (S1.08)
- Tarefas transversais S1: variaveis de ambiente, indices, middleware de log, tratamento de excecoes, CORS. (TR.01 a TR.05)

### Semana 2 — Jornada completa e ajustes

- Implementacao das quatro marcacoes com validacao de sequencia.
- Tela de solicitacao de ajuste de ponto com justificativa.
- Fluxo de aprovacao/rejeicao pelo ADM.
- Validacoes de negocio (nao permitir marcacao futura, bloquear ajustes sem justificativa, etc.).

### Semana 3 — Dashboard ADM e relatorios

- Dashboard do ADM com funcionarios sem ponto batido no dia.
- Lista de ajustes pendentes de analise.
- Relatorio de frequencia mensal com exportacao CSV.
- Filtros por periodo e funcionario.

### Semana 4 — Auditoria, refinamento e deploy

- Implementacao de logs de auditoria.
- Revisao de regras de seguranca e conformidade.
- Documentacao de uso para funcionario e ADM.
- Deploy da aplicacao em ambiente de nuvem (Railway, Render ou VPS proprio).
- Validacao final com piloto de poucos usuarios.

---

## 10. Estrutura do frontend (React)

O frontend e uma aplicacao de pagina unica (SPA) consumindo a API REST do backend.

### Paginas principais

- **Login**: autenticacao de funcionario e ADM.
- **Marcacao de ponto**: painel principal do funcionario com botoes de marcacao e resumo do dia.
- **Meu extrato**: consulta de marcacoes pessoais por periodo.
- **Solicitar ajuste**: formulario de correcao de ponto com justificativa.
- **Meus ajustes**: lista de solicitacoes proprias e seus status.
- **Dashboard ADM**: visao geral de frequencia do dia e ajustes pendentes.
- **Aprovar ajustes**: lista de solicitacoes pendentes com acoes de aprovar/rejeitar.
- **Cadastro de usuarios**: criacao e edicao de funcionarios (apenas ADM).
- **Relatorios**: geracao e exportacao de relatorios de frequencia (apenas ADM).

---

## 11. Estrutura do backend (FastAPI)

O backend expõe uma API REST organizada por dominio.

### Modulos principais

- **Autenticacao**: login, logout, geracao e validacao de JWT, hash de senha.
- **Usuarios**: CRUD de funcionarios e ADM, consulta de perfil, alteracao de status ativo/inativo.
- **Ponto**: registro de marcacoes, consulta de extrato, validacao de sequencia de marcacoes.
- **Ajustes**: criacao, listagem, aprovacao e rejeicao de solicitacoes de correticao.
- **Relatorios**: agregacao de dados de frequencia e geracao de CSV.
- **Auditoria**: gravacao e consulta de logs de acoes no sistema.

### Middlewares

- Autenticacao via Bearer Token.
- Log de requisicoes para rastreabilidade.
- Tratamento global de excecoes e respostas de erro padronizadas.
- CORS configurado para o dominio do frontend.

---

## 12. Banco de dados (PostgreSQL)

- Esquema relacional com chaves primarias e estrangeiras.
- Indices em colunas frequentemente consultadas (usuario, data da marcacao, status do ajuste).
- Controle de versao do esquema via Alembic (migracoes).
- Backups programados recomendados para ambiente de producao.

---

## 13. Variaveis de ambiente

A aplicacao deve ser configurada por meio de variaveis de ambiente, sem dados sensiveis no codigo.

Copie `.env.example` para `.env` e preencha os valores reais antes de rodar.

Exemplos de variaveis esperadas:

- URL de conexao com o banco de dados.
- Chave secreta para assinatura de tokens JWT.
- Tempo de expiracao do token.
- URL do frontend (para CORS).
- Nivel de log.
- Porta de execucao do backend e do frontend.

---

## 14. Execucao e deploy

### Desenvolvimento

1. Copie `.env.example` para `.env` e ajuste os valores.
2. Execute: `docker compose up --build`
3. O backend roda em `http://localhost:8000` (documentacao em `/docs`).
4. O frontend roda em `http://localhost:5173`.
5. O banco PostgreSQL roda na porta `5432`.

As migracoes Alembic sao aplicadas automaticamente na inicializacao do backend (`alembic upgrade head`).
O primeiro usuario ADM e criado automaticamente via seed (consulte `.env.example` para credenciais padrao).

### Producao

- Build do frontend gerando arquivos estaticos.
- Backend rodando com servidor ASGI (ex: Uvicorn) atras de proxy reverso (Nginx ou similar).
- Banco de dados PostgreSQL em instancia gerenciada ou contenedor com volume persistente.
- HTTPS obrigatorio (certificado SSL/TLS).
- Variaveis de ambiente injetadas via ambiente de execucao ou secrets da plataforma de nuvem.

---

## 15. Futuro: White label com banco separado

Quando o produto for oferecido para outras empresas, a arquitetura muda para **deploy isolado por cliente**:

- Cada empresa recebe sua propria instancia da aplicacao e seu proprio banco de dados PostgreSQL.
- Nao ha campo de empresa nas tabelas (nao e multitenant).
- Nao ha compartilhamento de dados entre empresas.
- Cada instalacao e configurada com as variaveis de ambiente de seu proprio banco.
- Atualizacoes sao aplicadas instancia por instancia.

---

## 16. Riscos e mitigacoes

| Risco | Mitigacao |
|-------|-----------|
| Usuario tenta burlar horario usando o relogio do navegador | Timestamp sempre gerado pelo servidor; frontend nao envia horario. |
| Exclusao acidental ou intencional de marcacoes | Sem endpoint de exclusao de ponto; correcoes apenas via ajuste com auditoria. |
| Acesso de funcionario a dados de outros funcionarios | Controle de autorizacao por perfil e por ID do usuario logado em cada requisicao. |
| Perda de dados do banco | Backups regulares e uso de PostgreSQL com durability configurada. |
| Vazamento de senhas | Hash criptografico, nunca texto plano; politicas de senhas fortes. |

---

## 17. Kanban

O acompanhamento das sprints esta em `kanban.md`. Cada tarefa tem ID (ex: S1.01), critério de aceitacao e coluna de status (Backlog, To Do, In Progress, Review/QA, Done, Blocked).

---

## 18. Licenca

Este projeto e de uso interno/restrito a empresa contratante ou desenvolvedora. Nao e software de codigo aberto por padrao.

---

## 19. Contato

Para duvidas sobre o escopo, funcionalidades ou conformidade com a Portaria 671, entre em contato com o time de produto ou com o responsavel tecnico pelo projeto.