import re
import requests
from typing import Optional


def validar_cnpj(cnpj: str) -> bool:
    """
    Valida um CNPJ utilizando algoritmo de dígitos verificadores.
    """
    # Remove caracteres não numéricos
    cnpj = re.sub(r'\D', '', cnpj)
    
    if len(cnpj) != 14:
        return False
    
    # Verifica se todos os dígitos são iguais (CNPJ inválido)
    if cnpj == cnpj[0] * 14:
        return False
    
    # Cálculo do primeiro dígito verificador
    def calcular_digito(cnpj_base: str, pesos: list) -> int:
        soma = sum(int(cnpj_base[i]) * pesos[i] for i in range(len(pesos)))
        resto = soma % 11
        return 0 if resto < 2 else 11 - resto
    
    # Pesos para o cálculo dos dígitos verificadores
    pesos_primeiro = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
    pesos_segundo = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
    
    # Calcula primeiro dígito
    primeiro_digito = calcular_digito(cnpj[:12], pesos_primeiro)
    
    # Calcula segundo dígito
    segundo_digito = calcular_digito(cnpj[:12] + str(primeiro_digito), pesos_segundo)
    
    return cnpj[-2:] == f"{primeiro_digito}{segundo_digito}"


def formatar_cnpj(cnpj: str) -> str:
    """
    Formata CNPJ no padrão XX.XXX.XXX/XXXX-XX
    """
    cnpj = re.sub(r'\D', '', cnpj)
    if len(cnpj) == 14:
        return f"{cnpj[:2]}.{cnpj[2:5]}.{cnpj[5:8]}/{cnpj[8:12]}-{cnpj[12:]}"
    return cnpj


def gerar_tenant_id(nome_empresa: str) -> str:
    """
    Gera um tenant_id a partir do nome da empresa.
    Converte para minúsculas, remove acentos e substitui espaços por hífens.
    """
    import unicodedata
    
    # Remove acentos
    nome_sem_acento = unicodedata.normalize('NFKD', nome_empresa).encode('ASCII', 'ignore').decode('ASCII')
    
    # Converte para minúsculas, remove caracteres especiais e substitui espaços por hífens
    tenant_id = re.sub(r'[^a-zA-Z0-9\s]', '', nome_sem_acento).lower()
    tenant_id = re.sub(r'\s+', '-', tenant_id).strip('-')
    
    return tenant_id


def testar_conexao_api(api_url: str, admin_key: str) -> bool:
    """
    Testa a conexão com a API verificando se o endpoint de tenants está acessível.
    """
    try:
        headers = {'X-Admin-Key': admin_key}
        response = requests.get(f"{api_url}/api/tenant", headers=headers, timeout=10)
        return response.status_code == 200
    except requests.exceptions.RequestException:
        return False


def testar_conexao_banco(db_config: dict) -> bool:
    """
    Testa a conexão com o banco de dados.
    """
    try:
        import psycopg2
        conn = psycopg2.connect(**db_config)
        conn.close()
        return True
    except Exception:
        return False


def verificar_tenant_existe(api_url: str, admin_key: str, tenant_id: str) -> bool:
    """
    Verifica se o tenant já existe na API.
    """
    try:
        headers = {'X-Admin-Key': admin_key}
        response = requests.get(f"{api_url}/api/tenant", headers=headers, timeout=10)
        
        if response.status_code == 200:
            tenants = response.json()
            return any(tenant['tenantId'] == tenant_id for tenant in tenants)
        return False
    except requests.exceptions.RequestException:
        return True  # Assume que existe se não conseguir verificar


def verificar_cnpj_existe(api_url: str, admin_key: str, cnpj: str) -> bool:
    """
    Verifica se o CNPJ já está cadastrado em algum tenant.
    """
    try:
        headers = {'X-Admin-Key': admin_key}
        response = requests.get(f"{api_url}/api/tenant", headers=headers, timeout=10)
        
        if response.status_code == 200:
            tenants = response.json()
            return any(tenant.get('cnpj') == cnpj for tenant in tenants)
        return False
    except requests.exceptions.RequestException:
        return True  # Assume que existe se não conseguir verificar