# Script de Criação de Tenants - MarcWeb

Este script automatiza a criação de novos tenants no sistema MarcWeb, substituindo o processo manual de criação de empresas.

## 📋 Pré-requisitos

### Dependências Python
Instale as dependências necessárias:
```bash
pip install -r scripts/requirements.txt
```

### Configuração do Banco de Dados
Antes de usar o script, é necessário criar as funções SQL no banco de dados:
```bash
psql -U marcuser -d marcweb_dev -f scripts/setup_tenant_db.sql
```

## ⚙️ Configuração

### Variáveis de Ambiente
Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```bash
# Configurações do banco de dados
DB_HOST=localhost
DB_PORT=5432
DB_NAME=marcweb_dev
DB_USER=marcuser
DB_PASSWORD=salfsa

# Configurações da API
API_BASE_URL=http://localhost:8080
ADMIN_KEY=marcweb_admin_2026

# Configurações padrão para empresas
DEFAULT_GEO_RADIUS=100
DEFAULT_GEO_ACTIVE=false
DEFAULT_PRECISION_MAX=500
DEFAULT_ADMIN_PASSWORD=admin
```

### Configurações Padrão
- **Raio de geolocalização**: 100 metros
- **Geolocalização ativa**: false
- **Precisão máxima**: 500 metros
- **Senha do admin**: admin

## 🚀 Uso

### Execução Básica
```bash
python scripts/criar_tenant.py
```

### Ajuda
```bash
python scripts/criar_tenant.py --help
```

## 📝 Fluxo do Script

1. **Coleta de Dados**: O script solicita interativamente:
   - Nome da empresa
   - CNPJ (com validação automática)
   - Tenant ID (gerado automaticamente ou personalizado)
   - Domínio (opcional)

2. **Validações**: O script verifica:
   - Conexão com a API
   - Conexão com o banco de dados
   - Disponibilidade do tenant ID
   - Disponibilidade do CNPJ

3. **Criação**: O script realiza:
   - Criação do tenant via API REST
   - Criação do schema no banco de dados
   - Criação das tabelas necessárias
   - Criação do usuário admin padrão

4. **Resultado**: Exibe as credenciais de acesso

## 📊 Estrutura de Arquivos

```
scripts/
├── criar_tenant.py          # Script principal (CLI interativo)
├── setup_tenant_db.sql      # Script SQL para criar schema e tabelas  
├── config.py               # Configurações de conexão e parâmetros
├── utils.py                # Funções auxiliares (validação CNPJ, etc.)
├── requirements.txt        # Lista de dependências Python
└── README.md              # Documentação de uso
```

## 🔧 Funcionalidades

### Validação de CNPJ
- Algoritmo de validação de dígitos verificadores
- Formatação automática no padrão XX.XXX.XXX/XXXX-XX

### Geração de Tenant ID
- Converte nome da empresa para minúsculas
- Remove acentos e caracteres especiais
- Substitui espaços por hífens
- Permite personalização pelo usuário

### Conexão com API
- Teste de conexão antes da criação
- Chamada ao endpoint `/api/tenant/create`
- Uso da chave de admin `marcweb_admin_2026`

### Criação de Schema
- Cria schema no formato `tenant_{tenant_id}`
- Cria todas as tabelas necessárias
- Insere configuração padrão da empresa
- Cria usuário admin com senha padrão

## 🛡️ Segurança

- **Senha do admin**: Definida nas configurações (padrão: `admin`)
- **Chave de admin**: Configurável via variável de ambiente
- **Validação de CNPJ**: Algoritmo robusto de validação
- **Verificação de duplicatas**: Impede tenants duplicados

## 📈 Exemplo de Uso

```bash
$ python scripts/criar_tenant.py

============================================================
           CRIADOR DE TENANTS MARCWEB
============================================================

📋 Por favor, informe os dados da empresa:
Nome da empresa: Acme Corporation
CNPJ: 12.345.678/0001-99
Tenant ID sugerido: acme-corporation
Tenant ID personalizado [Enter para usar o sugerido]: acme-corp
Domínio [opcional]: acme.com.br

🔍 Validando dados...
✅ Conexão com API: OK
✅ Conexão com banco de dados: OK
✅ Tenant ID 'acme-corp': disponível
✅ CNPJ '12.345.678/0001-99': disponível

⚠️  Deseja criar o tenant 'acme-corp' para a empresa 'Acme Corporation'?
Digite 'SIM' para confirmar: SIM

🌐 Criando tenant via API...
✅ Tenant criado na API com sucesso!

🗄️  Criando schema 'acme-corp' no banco de dados...
✅ Schema e tabelas criados com sucesso!

============================================================
           TENANT CRIADO COM SUCESSO!
============================================================
🏢 Nome da empresa: Acme Corporation
🆔 Tenant ID: acme-corp
🏢 Schema: tenant_acme-corp
📄 CNPJ: 12.345.678/0001-99
🌐 Domínio: acme.com.br
👤 Usuário admin: admin
🔑 Senha: admin
============================================================

💡 Para acessar o sistema:
   URL: http://localhost:8080
   Usuário: admin
   Senha: admin
   Tenant ID: acme-corp

🎉 Processo concluído com sucesso!
```

## 🔧 Personalização

### Configurações de Empresa
As configurações padrão podem ser alteradas no arquivo `config.py`:

```python
DEFAULT_GEO_RADIUS = 100        # Raio de geolocalização em metros
DEFAULT_GEO_ACTIVE = False      # Geolocalização ativa
DEFAULT_PRECISION_MAX = 500     # Precisão máxima em metros
DEFAULT_ADMIN_PASSWORD = 'admin' # Senha do admin
```

### Chave de Admin
Para alterar a chave de admin, atualize a variável `ADMIN_KEY` no `.env`:

```bash
ADMIN_KEY=sua_nova_chave_secreta
```

## 🐛 Troubleshooting

### Erros Comuns

1. **Falha na conexão com API**
   - Verifique se o servidor está rodando
   - Confira a URL da API no `.env`

2. **Falha na conexão com banco de dados**
   - Verifique as credenciais no `.env`
   - Confira se o PostgreSQL está rodando

3. **Tenant ID já existe**
   - Escolha um nome diferente para a empresa
   - Ou personalize o tenant ID

4. **CNPJ já cadastrado**
   - Verifique se o CNPJ já não está em uso
   - CNPJs devem ser únicos no sistema

### Logs
O script exibe mensagens detalhadas de progresso e erros. Para debug avançado, consulte os logs da aplicação backend.

## 📞 Suporte

Para suporte ou dúvidas sobre o script:
- Verifique o arquivo de logs da aplicação
- Consulte a documentação da API
- Verifique as configurações de banco de dados

## 📄 Licença

Este script é parte do projeto MarcWeb.