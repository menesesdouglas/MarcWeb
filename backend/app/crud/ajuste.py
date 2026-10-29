"""
CRUD de AjustePonto (S2.03, S2.04, S2.05).
"""
from datetime import date
from typing import Optional

from sqlalchemy.orm import Session

from app.models.ajuste import AjustePonto
from app.schemas.ajuste import AjusteCreate


def criar_solicitacao_ajuste(
    db: Session,
    usuario_id: int,
    ajuste_in: AjusteCreate,
) -> AjustePonto:
    """
    Cria uma solicitacao de ajuste de ponto com status pendente.
    """
    db_ajuste = AjustePonto(
        usuario_id=usuario_id,
        tipo_solicitado=ajuste_in.tipo_solicitado,
        data_solicitada=ajuste_in.data_solicitada,
        justificativa=ajuste_in.justificativa,
        status="pendente",
    )
    db.add(db_ajuste)
    db.commit()
    db.refresh(db_ajuste)
    return db_ajuste


def get_ajustes_por_usuario(
    db: Session,
    usuario_id: int,
) -> list[AjustePonto]:
    """Retorna as solicitacoes de ajuste de um usuario, mais recentes primeiro."""
    return (
        db.query(AjustePonto)
        .filter(AjustePonto.usuario_id == usuario_id)
        .order_by(AjustePonto.criado_em.desc())
        .all()
    )


def get_ajustes_pendentes(
    db: Session,
) -> list[AjustePonto]:
    """Retorna todas as solicitacoes pendentes (para o ADM analisar)."""
    return (
        db.query(AjustePonto)
        .filter(AjustePonto.status == "pendente")
        .order_by(AjustePonto.criado_em.asc())
        .all()
    )


def atualizar_status_ajuste(
    db: Session,
    ajuste_id: int,
    status: str,
    observacao_adm: Optional[str] = None,
) -> Optional[AjustePonto]:
    """Aprova ou rejeita um ajuste (apenas ADM).

    Bloqueia reprocessamento: um ajuste ja aprovado ou rejeitado nao pode
    ter o status alterado novamente (S2.05).
    """
    ajuste = (
        db.query(AjustePonto)
        .filter(AjustePonto.id == ajuste_id)
        .first()
    )
    if not ajuste:
        return None
    if ajuste.status != "pendente":
        raise ValueError(
            f"Ajuste #{ajuste_id} ja foi processado (status: {ajuste.status})."
        )
    if status not in ("aprovado", "rejeitado"):
        raise ValueError("Status invalido. Use 'aprovado' ou 'rejeitado'.")
    ajuste.status = status
    ajuste.observacao_adm = observacao_adm
    db.commit()
    db.refresh(ajuste)
    return ajuste