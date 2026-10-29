"""
Endpoints de ajuste de ponto (S2.03, S2.04).
"""
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.deps import DbSession, CurrentAdm, CurrentUser
from app.crud.ajuste import (
    criar_solicitacao_ajuste,
    get_ajustes_por_usuario,
    get_ajustes_pendentes,
    atualizar_status_ajuste,
)
from app.schemas.ajuste import (
    AjusteCreate,
    AjusteListOut,
    AjusteOut,
    AjusteStatusUpdate,
)

router = APIRouter()


@router.post(
    "/solicitar",
    response_model=AjusteListOut,
    status_code=status.HTTP_201_CREATED,
    summary="Solicitar ajuste de ponto",
)
def solicitar_ajuste(
    *,
    db: DbSession,
    current_user: CurrentUser,
    ajuste_in: AjusteCreate,
) -> AjusteListOut:
    """
    Solicita uma correcao de marcacao de ponto.
    A justificativa e obrigatória. O ajuste fica com status pendente
    ate ser analisado por um ADM.
    """
    ajuste = criar_solicitacao_ajuste(
        db=db,
        usuario_id=current_user.id,
        ajuste_in=ajuste_in,
    )
    return ajuste


@router.get(
    "/meus",
    response_model=list[AjusteListOut],
    summary="Meus ajustes de ponto",
)
def listar_meus_ajustes(
    *,
    db: DbSession,
    current_user: CurrentUser,
) -> list[AjusteListOut]:
    """Retorna as solicitacoes de ajuste do usuario logado."""
    return get_ajustes_por_usuario(db, current_user.id)


@router.get(
    "/pendentes",
    response_model=list[AjusteOut],
    summary="Ajustes pendentes para analise do ADM",
)
def listar_ajustes_pendentes(
    *,
    db: DbSession,
    current_adm: CurrentAdm,
) -> list[AjusteOut]:
    """Lista todas as solicitacoes de ajuste com status pendente (apenas ADM)."""
    return get_ajustes_pendentes(db)


@router.patch(
    "/{ajuste_id}/status",
    response_model=AjusteOut,
    summary="Aprova ou rejeita um ajuste de ponto (apenas ADM)",
)
def atualizar_status_ajuste_endpoint(
    *,
    db: DbSession,
    current_adm: CurrentAdm,
    ajuste_id: int,
    ajuste_in: AjusteStatusUpdate,
) -> AjusteOut:
    """Aprova ou rejeita um ajuste de ponto (apenas ADM).

    Bloqueia reprocessamento de ajuste ja aprovado ou rejeitado (S2.05).
    """
    try:
        ajuste = atualizar_status_ajuste(
            db=db,
            ajuste_id=ajuste_id,
            status=ajuste_in.status,
            observacao_adm=ajuste_in.observacao_adm,
        )
    except ValueError as exc:
        if "nao encontrado" in str(exc):
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Ajuste nao encontrado",
            )
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(exc),
        )
    if not ajuste:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ajuste nao encontrado",
        )
    return ajuste