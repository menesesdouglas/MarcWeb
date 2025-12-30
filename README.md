# marc-web

**Aplicação WEB de gestão de ponto eletrônico**  
Migração moderna do sistema Marc (original em Python) para web, com foco em segurança, conformidade CLT/LGPD e simplicidade para PMEs.

**Status atual:** Projeto iniciado – ponto zero (28/12/2025).  
Repositório criado, documentos de planejamento carregados (SDD.txt, SCRUM.txt, Configs.txt).  
Nenhum código implementado ainda.

## Objetivo Geral
Construir um sistema full-stack monolítico (Spring Boot + PostgreSQL + frontend leve com Pico CSS) em 18 sprints, dividido em 3 fases:
- Fase 1 (Sprints 1–4): Fundação local (setup, modelo de dados, entidades, segurança base).
- Fase 2 (Sprints 5–12): Core do negócio (ponto, relatórios, auditoria).
- Fase 3 (Sprints 13–18): Frontend completo + integrações + hardening.

## Próximos Passos – Início do Sprint 1 (29/12/2025)
**Sprint 1: Setup Geral & Infra Local**  
Objetivo: Ambiente 100% funcional em ambas as máquinas.

**Tarefas planejadas para amanhã:**
1. Confirmar configurações iniciais nas duas máquinas conforme Configs.txt (Java 17, Maven, PostgreSQL, Git, VS Code + extensões).
2. Criar banco de dados `marcweb_dev` e usuário `marcuser` em ambas as máquinas.
3. Criar repositório GitHub privado `marc-web` (se ainda não criado).
4. Clonar o repositório nas duas máquinas.
5. Gerar projeto Spring Boot via Spring Initializr:
   - Dependências: Spring Web, Spring Security, Spring Data JPA, PostgreSQL Driver, Lombok, Validation.
   - Java 17, Maven.
6. Configurar estrutura de pastas inicial conforme SDD (backend/src/main/..., static/, frontend/).
7. Adicionar profiles `application.yml` e `application-dev.yml` com conexão ao banco local.
8. Implementar endpoint simples `/api/health` para teste.
9. Primeiro commit e push.

**Marco do Sprint 1:** `mvn spring-boot:run` inicia a aplicação sem erros e conecta ao PostgreSQL.

## Histórico de Commits
(Nenhum commit realizado ainda – será atualizado diariamente ao final de cada sessão de trabalho.)

Este README será atualizado ao final de cada dia com:
- Resumo do que foi concluído.
- Commits realizados.
- Planejamento para o dia seguinte.

Pronto para começar o desenvolvimento.

**Dia 30/12/2025 08:43**

## Histórico de Commits

Sprint 1: Criado HealthApplication e testado com sucesso.

Marco Sprint 1 alcançado.
