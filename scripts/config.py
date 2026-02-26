import os
from dotenv import load_dotenv

# Carrega as variáveis de ambiente do .env
load_dotenv()

class Config:
    # Configurações do banco de dados
    DB_HOST = os.getenv('DB_HOST', 'localhost')
    DB_PORT = os.getenv('DB_PORT', '5432')
    DB_NAME = os.getenv('DB_NAME', 'marcweb_dev')
    DB_USER = os.getenv('DB_USER', 'marcuser')
    DB_PASSWORD = os.getenv('DB_PASSWORD', 'salfsa')
    
    # Configurações da API
    API_BASE_URL = os.getenv('API_BASE_URL', 'http://localhost:8080')
    ADMIN_KEY = os.getenv('ADMIN_KEY', 'marcweb_admin_2026')
    
    # Configurações padrão para empresas
    DEFAULT_GEO_RADIUS = int(os.getenv('DEFAULT_GEO_RADIUS', '100'))
    DEFAULT_GEO_ACTIVE = os.getenv('DEFAULT_GEO_ACTIVE', 'false').lower() == 'true'
    DEFAULT_PRECISION_MAX = int(os.getenv('DEFAULT_PRECISION_MAX', '500'))
    
    # Configurações de senha padrão
    DEFAULT_ADMIN_PASSWORD = os.getenv('DEFAULT_ADMIN_PASSWORD', 'admin')

# Configurações de conexão com o banco de dados
DB_CONFIG = {
    'host': Config.DB_HOST,
    'port': Config.DB_PORT,
    'database': Config.DB_NAME,
    'user': Config.DB_USER,
    'password': Config.DB_PASSWORD
}

# Tornar DB_CONFIG acessível como atributo da classe
Config.DB_CONFIG = DB_CONFIG
