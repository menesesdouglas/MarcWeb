"""
CRUD de RegistroPonto (S1.07, S1.08).
"""
from datetime import date, datetime, timedelta, timezone
from typing import Optional

from sqlalchemy.orm import Session

from app.models.registro_ponto import RegistroPonto
from app.models.usuario import Usuario


def criar_registro_ponto(
    db: Session,
    usuario: Usuario,
    tipo: str,
    ip_origem: str | None = None,
) -> RegistroPonto:
    """
    Cria uma nova marcação de ponto. O timestamp e gerado exclusivamente pelo servidor.
    """
    db_registro = RegistroPonto(
        usuario_id=usuario.id,
        tipo=tipo,
        ip_origem=ip_origem,
        origem="web",
    )
    db.add(db_registro)
    db.commit()
    db.refresh(db_registro)
    return db_registro


def get_registros_por_usuario_dia(
    db: Session,
    usuario_id: int,
    dia: date,
) -> list[RegistroPonto]:
    """
    Retorna as marcações de um usuário em um dia específico.
    """
    inicio = datetime(dia.year, dia.month, dia.day, tzinfo=timezone.utc)
    fim = inicio + timedelta(days=1)
    return (
        db.query(RegistroPonto)
        .filter(
            RegistroPonto.usuario_id == usuario_id,
            RegistroPonto.timestamp >= inicio,
            RegistroPonto.timestamp < fim,
        )
        .order_by(RegistroPonto.timestamp.asc())
        .all()
    )


def get_ultimo_registro_por_usuario(
    db: Session,
    usuario_id: int,
) -> Optional[RegistroPonto]:
    """Retorna a última marcação do usuário."""
    return (
        db.query(RegistroPonto)
        .filter(RegistroPonto.usuario_id == usuario_id)
        .order_by(RegistroPonto.timestamp.desc())
        .first()
    )