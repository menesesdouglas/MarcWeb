"""
Modelo RegistroPonto (README Seção 6.2).
Timestamp gerado exclusivamente pelo servidor. Registros são imutáveis (não excluídos).
"""
from datetime import datetime
from typing import Optional

from sqlalchemy import DateTime, ForeignKey, String, func, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class RegistroPonto(Base):
    __tablename__ = "registros_ponto"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    usuario_id: Mapped[int] = mapped_column(
        ForeignKey("usuarios.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    tipo: Mapped[str] = mapped_column(String(30), nullable=False)  # entrada, saida_intervalo, retorno_intervalo, saida
    timestamp: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False, index=True
    )
    ip_origem: Mapped[Optional[str]] = mapped_column(String(45), nullable=True)
    origem: Mapped[str] = mapped_column(String(20), nullable=False, default="web")  # 'web'

    created_at: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=True
    )

    # Relacionamento
    usuario: Mapped["Usuario"] = relationship("Usuario", back_populates="registros_ponto")

    # Índices compostos para consultas frequentes (TR.02)
    __table_args__ = (
        Index("ix_registros_ponto_usuario_data", "usuario_id", "timestamp"),
    )

    def __repr__(self) -> str:
        return f"<RegistroPonto(id={self.id}, usuario_id={self.usuario_id}, tipo={self.tipo}, timestamp={self.timestamp})>"