"""
Schemas de Usuario (S1.05).
Validação de unicidade de e-mail e matrícula é feita no CRUD/banco.
"""
from datetime import datetime
from typing import Optional
from uuid import UUID

from pydantic import BaseModel, EmailStr, Field


class UsuarioBase(BaseModel):
    nome: str = Field(..., min_length=2, max_length=200)
    email: EmailStr
    cpf: str = Field(..., min_length=11, max_length=20)
    matricula: str = Field(..., min_length=1, max_length=50)
    perfil: str = Field(..., pattern="^(funcionario|adm)$")
    status: str = Field(default="ativo", pattern="^(ativo|inativo)$")


class UsuarioCreate(UsuarioBase):
    senha: str = Field(..., min_length=6, max_length=128)


class UsuarioUpdate(BaseModel):
    nome: Optional[str] = Field(default=None, min_length=2, max_length=200)
    email: Optional[EmailStr] = None
    cpf: Optional[str] = Field(default=None, min_length=11, max_length=20)
    matricula: Optional[str] = Field(default=None, min_length=1, max_length=50)
    perfil: Optional[str] = Field(default=None, pattern="^(funcionario|adm)$")
    status: Optional[str] = Field(default=None, pattern="^(ativo|inativo)$")


class UsuarioOut(UsuarioBase):
    id: int
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None

    # Senha nunca é exposta
    senha_hash: Optional[str] = None

    model_config = {"from_attributes": True}


class UsuarioListOut(BaseModel):
    id: int
    nome: str
    email: str
    cpf: str
    matricula: str
    perfil: str
    status: str
    created_at: Optional[datetime] = None

    model_config = {"from_attributes": True}