#!/usr/bin/env python3
"""
Script para ajustar a senha do admin de tenants existentes
Autor: Cline AI
Data: 2026
"""

import sys
import os
import bcrypt
from typing import Optional

# Adiciona o diretório scripts ao path para importar os módulos locais
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from config import Config
import psycopg2


class AjustadorAdmin:
    def __init__(self):
        self.config = Config()
        
    def exibir_cabecalho(self):
        """Exibe o cabeçalho do script"""
        print("=" * 60)
        print("           AJUSTADOR DE SENHA DO ADMIN")
        print("=" * 60)
        print()
    
    def gerar_hash_senha(self, senha: str) -> str:
        """Gera hash BCrypt para a senha fornecida"""
        hash_senha = bcrypt.hashpw(senha.encode('utf-8'), bcrypt.gensalt(rounds=12))
        return hash_senha.decode('utf-8')
    
    def obter_tenants_existentes(self) -> list:
        """Obtém a lista de tenants existentes"""
        try:
            conn = psycopg2.connect(**Config.DB_CONFIG)
            cursor = conn.cursor()
            
            cursor.execute("SELECT tenant_id FROM tenants ORDER BY tenant_id")
            tenants = [row[0] for row in cursor.fetchall()]
            
            cursor.close()
            conn.close()
            
            return tenants
            
        except Exception as e:
            print(f"❌ Erro ao obter tenants existentes: {e}")
            return []
    
    def ajustar_senha_admin(self, tenant_id: str, senha: str) -> bool:
        """Ajusta a senha do admin para um tenant específico"""
        print(f"\n🔐 Ajustando senha do admin para o tenant '{tenant_id}'...")
        
        try:
            conn = psycopg2.connect(**Config.DB_CONFIG)
            cursor = conn.cursor()
            
            # Gera o hash da senha
            hash_senha = self.gerar_hash_senha(senha)
            
            # Atualiza a senha do admin no schema do tenant
            cursor.execute(f"""
                UPDATE tenant_{tenant_id}.users 
                SET password = %s 
                WHERE username = 'admin'
            """, (hash_senha,))
            
            # Verifica se a atualização foi bem-sucedida
            if cursor.rowcount > 0:
                conn.commit()
                print(f"✅ Senha do admin atualizada com sucesso para o tenant '{tenant_id}'")
                return True
            else:
                print(f"❌ Usuário admin não encontrado no tenant '{tenant_id}'")
                return False
                
        except Exception as e:
            print(f"❌ Erro ao ajustar senha do admin: {e}")
            return False
        finally:
            try:
                cursor.close()
                conn.close()
            except:
                pass
    
    def exibir_informacoes(self, tenant_id: str, senha: str):
        """Exibe informações sobre o ajuste realizado"""
        print("\n" + "=" * 60)
        print("           SENHA DO ADMIN AJUSTADA!")
        print("=" * 60)
        print(f"🆔 Tenant ID: {tenant_id}")
        print(f"👤 Usuário: admin")
        print(f"🔑 Senha: {senha}")
        print("=" * 60)
        print("\n💡 Para testar o login:")
        print(f"   URL: {self.config.API_BASE_URL}")
        print(f"   Usuário: admin")
        print(f"   Senha: {senha}")
        print(f"   Tenant ID: {tenant_id}")
        print()
    
    def executar(self):
        """Executa o fluxo completo de ajuste de senha"""
        self.exibir_cabecalho()
        
        try:
            # Obtém a lista de tenants
            tenants = self.obter_tenants_existentes()
            
            if not tenants:
                print("❌ Nenhum tenant encontrado no banco de dados.")
                return False
            
            print("🏢 Tenants existentes:")
            for i, tenant in enumerate(tenants, 1):
                print(f"   {i}. {tenant}")
            
            # Seleciona o tenant
            while True:
                try:
                    escolha = input(f"\nSelecione o tenant (1-{len(tenants)}) ou 'todos' para ajustar todos: ").strip()
                    
                    if escolha.lower() == 'todos':
                        tenants_selecionados = tenants
                        break
                    else:
                        indice = int(escolha) - 1
                        if 0 <= indice < len(tenants):
                            tenants_selecionados = [tenants[indice]]
                            break
                        else:
                            print("❌ Opção inválida. Tente novamente.")
                except ValueError:
                    print("❌ Opção inválida. Tente novamente.")
            
            # Define a senha
            senha = input("Digite a nova senha para o admin: ").strip()
            if not senha:
                print("❌ A senha não pode ser vazia.")
                return False
            
            # Confirmação
            print(f"\n⚠️  Deseja ajustar a senha do admin para {len(tenants_selecionados)} tenant(s)?")
            confirmacao = input("Digite 'SIM' para confirmar: ").strip().upper()
            
            if confirmacao != 'SIM':
                print("❌ Operação cancelada pelo usuário.")
                return False
            
            # Ajusta as senhas
            sucesso_total = True
            for tenant_id in tenants_selecionados:
                sucesso = self.ajustar_senha_admin(tenant_id, senha)
                if not sucesso:
                    sucesso_total = False
            
            if sucesso_total:
                print(f"\n🎉 Processo concluído com sucesso para {len(tenants_selecionados)} tenant(s)!")
                if len(tenants_selecionados) == 1:
                    self.exibir_informacoes(tenants_selecionados[0], senha)
                return True
            else:
                print(f"\n💥 Processo falhou para alguns tenants!")
                return False
                
        except KeyboardInterrupt:
            print("\n\n❌ Operação cancelada pelo usuário.")
            return False
        except Exception as e:
            print(f"\n❌ Erro inesperado: {e}")
            return False


def main():
    """Função principal"""
    if len(sys.argv) > 1 and sys.argv[1] in ['--help', '-h']:
        print("Uso: python ajustar_admin.py")
        print("Ajusta a senha do admin para tenants existentes.")
        sys.exit(0)
    
    ajustador = AjustadorAdmin()
    sucesso = ajustador.executar()
    
    if sucesso:
        print("🎉 Processo concluído com sucesso!")
        sys.exit(0)
    else:
        print("💥 Processo falhou!")
        sys.exit(1)


if __name__ == "__main__":
    main()