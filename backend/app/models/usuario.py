"""
Modelo Usuario (README Seção 6.1).
Perfis: 'funcionario' ou 'adm'. Status: 'ativo' ou 'inativo'.
"""
from datetime import datetime
from typing import Optional

from sqlalchemy import Boolean, DateTime, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class Usuario(Base):
    __tablename__ = "usuarios"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    nome: Mapped[str] = mapped_column(String(200), nullable=False)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    senha_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    cpf: Mapped[str] = mapped_column(String(20), nullable=False)
    matricula: Mapped[str] = mapped_column(String(50), unique=True, index=True, nullable=False)
    perfil: Mapped[str] = mapped_column(String(20), nullable=False)  # 'funcionario' | 'adm'
    status: Mapped[str] = mapped_column(String(20), nullable=False, default="ativo")  # 'ativo' | 'inativo'

    created_at: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=True
    )
    updated_at: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=True,
    )

    # Relacionamentos
    registros_ponto: Mapped[list["RegistroPonto"]] = relationship(
        "RegistroPonto", back_populates="usuario", cascade="all, delete-orphan"
    )
    ajustes_ponto: Mapped[list["AjustePonto"]] = relationship(
        "AjustePonto", back_populates="usuario", cascade="all, delete-orphan"
    )

    def __repr__(self) -> str:
        return f"<Usuario(id={self.id}, email={self.email}, perfil={self.perfil})>"