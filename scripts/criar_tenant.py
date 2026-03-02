#!/usr/bin/env python3

import sys
import os
import getpass
import bcrypt
from typing import Dict, Any, Optional

# Adiciona o diretório scripts ao path para importar os módulos locais
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from config import Config
from utils import (
    validar_cnpj, formatar_cnpj, gerar_tenant_id,
    testar_conexao_api, testar_conexao_banco,
    verificar_tenant_existe, verificar_cnpj_existe
)
import requests
import psycopg2


class CriadorTenant:
    def __init__(self):
        self.config = Config()
        
    def exibir_cabecalho(self):
        """Exibe o cabeçalho do script"""
        print("=" * 60)
        print("           CRIADOR DE TENANTS MARCWEB")
        print("=" * 60)
        print()
    
    def obter_plano(self) -> str:
        """Exibe menu para seleção do plano"""
        print("\n📋 Selecione o plano:")
        print("  1 - Freemium (Gratuito)")
        print("  2 - Básico")
        print("  3 - Premium")
        print("  4 - Enterprise")
        print()
        
        planos = {
            "1": "FREEMIUM",
            "2": "BASICO", 
            "3": "PREMIUM",
            "4": "ENTERPRISE"
        }
        
        while True:
            opcao = input("Digite o número do plano [1]: ").strip()
            if not opcao:
                return "FREEMIUM"  # Default
            if opcao in planos:
                return planos[opcao]
            print("❌ Opção inválida. Digite 1, 2, 3 ou 4.")
    
    def obter_dados_empresa(self) -> Dict[str, str]:
        """Coleta os dados da empresa interativamente"""
        dados = {}
        
        # Nome da empresa
        while True:
            nome = input("Nome da empresa: ").strip()
            if nome:
                dados['nome'] = nome
                dados['tenant_id'] = gerar_tenant_id(nome)
                break
            print("❌ O nome da empresa é obrigatório.")
        
        # CNPJ
        while True:
            cnpj = input("CNPJ: ").strip()
            if cnpj:
                if validar_cnpj(cnpj):
                    dados['cnpj'] = formatar_cnpj(cnpj)
                    break
                else:
                    print("❌ CNPJ inválido. Por favor, digite um CNPJ válido.")
            else:
                print("❌ O CNPJ é obrigatório.")
        
        # Tenant ID (permitir personalização)
        print(f"Tenant ID sugerido: {dados['tenant_id']}")
        tenant_custom = input("Tenant ID personalizado [Enter para usar o sugerido]: ").strip()
        if tenant_custom:
            dados['tenant_id'] = tenant_custom
        
        # Domínio (opcional)
        dominio = input("Domínio [opcional]: ").strip()
        dados['dominio'] = dominio if dominio else None
        
        # Plano
        dados['plano'] = self.obter_plano()
        
        return dados
    
    def validar_dados(self, dados: Dict[str, str]) -> bool:
        """Valida os dados coletados"""
        print("\n🔍 Validando dados...")
        
        # Testa conexão com a API
        if not testar_conexao_api(self.config.API_BASE_URL, self.config.ADMIN_KEY):
            print("❌ Falha ao conectar com a API. Verifique se o servidor está rodando.")
            return False
        print("✅ Conexão com API: OK")
        
        # Testa conexão com o banco de dados
        if not testar_conexao_banco(Config.DB_CONFIG):
            print("❌ Falha ao conectar com o banco de dados. Verifique as configurações.")
            return False
        print("✅ Conexão com banco de dados: OK")
        
        # Verifica se o tenant_id já existe
        if verificar_tenant_existe(self.config.API_BASE_URL, self.config.ADMIN_KEY, dados['tenant_id']):
            print(f"❌ O tenant ID '{dados['tenant_id']}' já existe.")
            return False
        print(f"✅ Tenant ID '{dados['tenant_id']}': disponível")
        
        # Verifica se o CNPJ já está cadastrado
        if verificar_cnpj_existe(self.config.API_BASE_URL, self.config.ADMIN_KEY, dados['cnpj']):
            print(f"❌ O CNPJ '{dados['cnpj']}' já está cadastrado em outro tenant.")
            return False
        print(f"✅ CNPJ '{dados['cnpj']}': disponível")
        
        return True
    
    def obter_token_jwt(self) -> Optional[str]:
        """Obtém token JWT fazendo login na API"""
        print("\n🔑 Obtendo token JWT...")
        
        payload = {
            "username": "admin",
            "password": "admin123"
        }
        
        headers = {
            'Content-Type': 'application/json'
        }
        
        try:
            response = requests.post(
                f"{self.config.API_BASE_URL}/api/auth/login",
                json=payload,
                headers=headers,
                timeout=30
            )
            
            if response.status_code == 200:
                data = response.json()
                token = data.get('token')
                if token:
                    print("✅ Token JWT obtido com sucesso!")
                    return token
                else:
                    print("❌ Token não encontrado na resposta")
                    return None
            else:
                print(f"❌ Falha ao obter token JWT: {response.status_code}")
                print(f"   Resposta: {response.text}")
                return None
                
        except requests.exceptions.RequestException as e:
            print(f"❌ Erro ao obter token JWT: {e}")
            return None
    
    def criar_tenant_api(self, dados: Dict[str, str]) -> bool:
        """Cria o tenant via API REST"""
        print("\n🌐 Criando tenant via API...")
        
        # Primeiro obtém o token JWT
        token = self.obter_token_jwt()
        if not token:
            return False
        
        payload = {
            "tenantId": dados['tenant_id'],
            "nome": dados['nome'],
            "cnpj": dados['cnpj'],
            "dominio": dados['dominio'],
            "plano": dados['plano']
        }
        
        headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {token}',
            'X-Admin-Key': self.config.ADMIN_KEY
        }
        
        try:
            response = requests.post(
                f"{self.config.API_BASE_URL}/api/tenant/create",
                json=payload,
                headers=headers,
                timeout=30
            )
            
            if response.status_code == 201:
                print("✅ Tenant criado na API com sucesso!")
                return True
            else:
                print(f"❌ Falha ao criar tenant na API: {response.status_code}")
                print(f"   Resposta: {response.text}")
                return False
                
        except requests.exceptions.RequestException as e:
            print(f"❌ Erro ao comunicar com a API: {e}")
            return False
    
    def criar_schema_banco(self, tenant_id: str) -> bool:
        """Cria o schema e tabelas no banco de dados"""
        print(f"\n🗄️  Criando schema '{tenant_id}' no banco de dados...")
        
        try:
            conn = psycopg2.connect(**Config.DB_CONFIG)
            conn.autocommit = True
            cursor = conn.cursor()
            
            # Remove o schema se já existir (para recriar limpo) - usando format seguro
            schema_name = f"tenant_{tenant_id}"
            cursor.execute(f"DROP SCHEMA IF EXISTS {schema_name} CASCADE")
            print(f"✅ Schema existente removido (se havia)")
            
            # Cria o schema
            cursor.execute(f"CREATE SCHEMA {schema_name}")
            print(f"✅ Schema '{schema_name}' criado")
            
            # Cria a tabela users com ativacoes
            cursor.execute(f"""
                CREATE TABLE {schema_name}.users (
                    id BIGSERIAL PRIMARY KEY,
                    tenant_id VARCHAR(50) NOT NULL,
                    nome VARCHAR(150) NOT NULL,
                    username VARCHAR(100) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    ativo BOOLEAN DEFAULT TRUE,
                    consent_geoloc BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    cargo VARCHAR(100),
                    cpf VARCHAR(20),
                    ativacoes INTEGER DEFAULT 1 NOT NULL
                )
            """)
            print(f"✅ Tabela users criada com coluna ativacoes")
            
            # Cria índices
            cursor.execute(f"CREATE UNIQUE INDEX idx_users_tenant_username ON {schema_name}.users(tenant_id, username)")
            cursor.execute(f"CREATE INDEX idx_users_tenant_id ON {schema_name}.users(tenant_id)")
            
            # Cria tabela configs_empresa
            cursor.execute(f"""
                CREATE TABLE {schema_name}.configs_empresa (
                    id BIGSERIAL PRIMARY KEY,
                    nome_empresa VARCHAR(200) NOT NULL,
                    lat_centro DOUBLE PRECISION,
                    long_centro DOUBLE PRECISION,
                    raio_geoloc INTEGER DEFAULT 100,
                    geoloc_ativa BOOLEAN DEFAULT FALSE,
                    precisao_maxima INTEGER DEFAULT 500
                )
            """)
            
            # Insere configuração padrão
            cursor.execute(f"INSERT INTO {schema_name}.configs_empresa (nome_empresa, raio_geoloc, geoloc_ativa) VALUES ('Empresa {tenant_id}', 100, false)")
            
            # Cria outras tabelas...
            cursor.execute(f"""
                CREATE TABLE {schema_name}.registros_ponto (
                    id BIGSERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    tipo VARCHAR(30) NOT NULL,
                    timestamp TIMESTAMP NOT NULL,
                    latitude DOUBLE PRECISION,
                    longitude DOUBLE PRECISION,
                    ip VARCHAR(45),
                    precisao DOUBLE PRECISION,
                    justificativa TEXT,
                    expediente_id UUID
                )
            """)
            cursor.execute(f"CREATE INDEX idx_registro_user_data ON {schema_name}.registros_ponto(user_id, timestamp)")
            
            cursor.execute(f"""
                CREATE TABLE {schema_name}.feriados (
                    id BIGSERIAL PRIMARY KEY,
                    data DATE NOT NULL,
                    nome VARCHAR(150) NOT NULL,
                    origem VARCHAR(20) NOT NULL
                )
            """)
            cursor.execute(f"CREATE INDEX idx_feriados_data ON {schema_name}.feriados(data)")
            
            cursor.execute(f"""
                CREATE TABLE {schema_name}.folgas (
                    id BIGSERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    data_inicio DATE NOT NULL,
                    data_fim DATE NOT NULL,
                    tipo VARCHAR(20) DEFAULT 'FOLGA',
                    status VARCHAR(20) DEFAULT 'Pendente',
                    justificativa TEXT,
                    dias_calculados INTEGER DEFAULT 0,
                    aprovada BOOLEAN
                )
            """)
            
            cursor.execute(f"""
                CREATE TABLE {schema_name}.folhas_ponto (
                    id BIGSERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    mes INTEGER NOT NULL,
                    ano INTEGER NOT NULL,
                    total_horas_trabalhadas VARCHAR(10),
                    total_horas_extras VARCHAR(10),
                    total_horas_faltantes VARCHAR(10),
                    status VARCHAR(20) DEFAULT 'Pendente',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(user_id, mes, ano)
                )
            """)
            
            cursor.execute(f"""
                CREATE TABLE {schema_name}.logs_auditoria (
                    id BIGSERIAL PRIMARY KEY,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    user_id BIGINT,
                    acao VARCHAR(150) NOT NULL,
                    categoria VARCHAR(30) NOT NULL,
                    detalhes TEXT,
                    status VARCHAR(20) NOT NULL
                )
            """)
            cursor.execute(f"CREATE INDEX idx_logs_user_data ON {schema_name}.logs_auditoria(user_id, timestamp)")
            
            print(f"✅ Todas as tabelas criadas com sucesso!")
            
            # Cria o usuário admin com senha criptografada
            self.criar_usuario_admin(cursor, tenant_id)
            
            cursor.close()
            conn.close()
            
            print("✅ Schema, tabelas e usuário admin criados com sucesso!")
            return True
            
        except Exception as e:
            print(f"❌ Erro ao criar schema no banco de dados: {e}")
            import traceback
            traceback.print_exc()
            return False
    
    def criar_usuario_admin(self, cursor, tenant_id: str):
        """Cria o usuário admin com senha criptografada"""
        print(f"🔐 Criando usuário admin para o tenant '{tenant_id}'...")
        
        schema_name = f"tenant_{tenant_id}"
        
        # Gera o hash BCrypt para a senha padrão
        senha_plana = self.config.DEFAULT_ADMIN_PASSWORD
        hash_senha = bcrypt.hashpw(senha_plana.encode('utf-8'), bcrypt.gensalt(rounds=12))
        senha_hash = hash_senha.decode('utf-8')
        
        # Insere o usuário admin
        cursor.execute(f"""
            INSERT INTO {schema_name}.users (username, password, role, tenant_id, nome, ativo, created_at, ativacoes)
            VALUES ('admin', '{senha_hash}', 'ADMIN', '{tenant_id}', 'Administrador', true, NOW(), 1)
        """)
        
        print(f"✅ Usuário admin criado com senha criptografada para o tenant '{tenant_id}'")
    
    def exibir_resultado(self, dados: Dict[str, str]):
        """Exibe o resultado final da criação do tenant"""
        print("\n" + "=" * 60)
        print("           TENANT CRIADO COM SUCESSO!")
        print("=" * 60)
        print(f"🏢 Nome da empresa: {dados['nome']}")
        print(f"🆔 Tenant ID: {dados['tenant_id']}")
        print(f"🏢 Schema: tenant_{dados['tenant_id']}")
        print(f"📄 CNPJ: {dados['cnpj']}")
        if dados['dominio']:
            print(f"🌐 Domínio: {dados['dominio']}")
        print(f"📦 Plano: {dados['plano']}")
        print(f"👤 Usuário admin: admin")
        print(f"🔑 Senha: {self.config.DEFAULT_ADMIN_PASSWORD}")
        print("=" * 60)
        print("\n💡 Para acessar o sistema:")
        print(f"   URL: {self.config.API_BASE_URL}")
        print(f"   Usuário: admin")
        print(f"   Senha: {self.config.DEFAULT_ADMIN_PASSWORD}")
        print(f"   Tenant ID: {dados['tenant_id']}")
        print()
    
    def executar(self):
        """Executa o fluxo completo de criação de tenant"""
        self.exibir_cabecalho()
        
        try:
            # Coleta os dados da empresa
            print("📋 Por favor, informe os dados da empresa:")
            dados = self.obter_dados_empresa()
            print()
            
            # Valida os dados
            if not self.validar_dados(dados):
                print("\n❌ Falha na validação dos dados. Operação cancelada.")
                return False
            
            # Confirmação antes de criar
            print(f"\n⚠️  Deseja criar o tenant '{dados['tenant_id']}' para a empresa '{dados['nome']}'?")
            confirmacao = input("Digite 'SIM' para confirmar: ").strip().upper()
            
            if confirmacao != 'SIM':
                print("❌ Operação cancelada pelo usuário.")
                return False
            
            # Cria o tenant via API
            if not self.criar_tenant_api(dados):
                print("\n❌ Falha ao criar tenant via API. Operação cancelada.")
                return False
            
            # Cria o schema no banco de dados
            if not self.criar_schema_banco(dados['tenant_id']):
                print("\n❌ Falha ao criar schema no banco de dados.")
                print("⚠️  O tenant foi criado na API, mas o schema não foi criado no banco.")
                print("💡 Você pode tentar criar o schema manualmente usando o script setup_tenant_db.sql")
                return False
            
            # Exibe o resultado
            self.exibir_resultado(dados)
            return True
            
        except KeyboardInterrupt:
            print("\n\n❌ Operação cancelada pelo usuário.")
            return False
        except Exception as e:
            print(f"\n❌ Erro inesperado: {e}")
            import traceback
            traceback.print_exc()
            return False


def main():
    """Função principal"""
    if len(sys.argv) > 1 and sys.argv[1] in ['--help', '-h']:
        print("Uso: python criar_tenant.py")
        print("Cria um novo tenant no sistema MarcWeb de forma interativa.")
        sys.exit(0)
    
    criador = CriadorTenant()
    sucesso = criador.executar()
    
    if sucesso:
        print("🎉 Processo concluído com sucesso!")
        sys.exit(0)
    else:
        print("💥 Processo falhou!")
        sys.exit(1)


if __name__ == "__main__":
    main()
