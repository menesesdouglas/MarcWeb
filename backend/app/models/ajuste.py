"""
Modelo AjustePonto (README Seção 6, S2.03).
Solicitacao de correcao de marcacao de ponto com justificativa obrigatoria.
Fluxo: pendente -> aprovado ou rejeitado pelo ADM.
"""
from datetime import date, datetime
from typing import Optional

from sqlalchemy import Date, DateTime, ForeignKey, String, func, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class AjustePonto(Base):
    __tablename__ = "ajustes_ponto"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    usuario_id: Mapped[int] = mapped_column(
        ForeignKey("usuarios.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    tipo_solicitado: Mapped[str] = mapped_column(String(30), nullable=False)
    data_solicitada: Mapped[date] = mapped_column(
        Date, nullable=False, index=True
    )
    justificativa: Mapped[str] = mapped_column(String(1000), nullable=False)
    status: Mapped[str] = mapped_column(
        String(20), nullable=False, default="pendente"
    )  # pendente | aprovado | rejeitado
    observacao_adm: Mapped[Optional[str]] = mapped_column(
        String(1000), nullable=True
    )
    criado_em: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=True
    )
    atualizado_em: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=True,
    )

    # Relacionamento
    usuario: Mapped["Usuario"] = relationship(
        "Usuario", back_populates="ajustes_ponto"
    )

    __table_args__ = (
        Index("ix_ajustes_ponto_usuario_status", "usuario_id", "status"),
    )

    def __repr__(self) -> str:
        return (
            f"<AjustePonto(id={self.id}, usuario_id={self.usuario_id}, "
            f"tipo={self.tipo_solicitado}, status={self.status})>"
        )