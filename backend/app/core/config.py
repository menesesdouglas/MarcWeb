"""
Configurações da aplicação via variáveis de ambiente (TR.01).
Utiliza pydantic-settings para carregar .env e variáveis de ambiente do SO.
"""
from functools import lru_cache
from typing import List, Union

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # --- Banco de dados ---
    DATABASE_URL: str = "postgresql+psycopg2://repp:repp_password@localhost:5432/repp"

    # --- Segurança / JWT ---
    SECRET_KEY: str = "dev-secret-change-in-production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30

    # --- CORS ---
    FRONTEND_URL: str = "http://localhost:5173"
    BACKEND_CORS_ORIGINS: List[str] = ["http://localhost:5173"]

    # --- Servidor ---
    BACKEND_PORT: int = 8000
    LOG_LEVEL: str = "INFO"

    # --- Seed do primeiro ADM (opcional) ---
    ADM_EMAIL: str = "adm@empresa.com"
    ADM_PASSWORD: str = "Adm@123456"
    ADM_NOME: str = "Administrador Sistema"
    ADM_CPF: str = "000.000.000-00"
    ADM_MATRICULA: str = "ADM0001"

    model_config = {
        "env_file": ".env",
        "env_file_encoding": "utf-8",
        "case_sensitive": True,
        "extra": "ignore",
    }


@lru_cache()
def get_settings() -> Settings:
    """Retorna instância cacheada das configurações."""
    return Settings()


settings = get_settings()