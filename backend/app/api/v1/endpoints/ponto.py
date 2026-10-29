"""
Endpoints de marcacao de ponto e extrato (S1.07, S1.08, S2.02).
"""
from datetime import date

from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy.orm import Session

from app.api.deps import DbSession, CurrentUser
from app.crud.registro_ponto import (
    criar_registro_ponto,
    get_registros_por_usuario_dia,
    get_ultimo_registro_por_usuario,
)
from app.schemas.registro_ponto import (
    ExtratoDiaOut,
    MarcacaoResumoOut,
    RegistroPontoCreate,
    RegistroPontoOut,
)

router = APIRouter()

TIPO_PROXIMO_ESPERADO = {
    None: "entrada",
    "entrada": "saida_intervalo",
    "saida_intervalo": "retorno_intervalo",
    "retorno_intervalo": "saida",
    "saida": "entrada",
}


@router.post(
    "/marcar",
    response_model=RegistroPontoOut,
    status_code=status.HTTP_201_CREATED,
    summary="Marca ponto (entrada, saida intervalo, retorno intervalo ou saida final)",
)
def marcar_ponto(
    *,
    db: DbSession,
    current_user: CurrentUser,
    registro_in: RegistroPontoCreate,
    request: Request,
) -> RegistroPontoOut:
    """
    Registra uma marcacao de ponto entre os quatro tipos padrao.
    O timestamp e gerado exclusivamente pelo servidor.
    Endereco IP e capturado da requisicao.
    Valida a sequencia de marcacoes do usuario (S2.02).
    """
    ip_origem = request.client.host if request.client else None

    try:
        registro = criar_registro_ponto(
            db=db,
            usuario=current_user,
            tipo=registro_in.tipo,
            ip_origem=ip_origem,
        )
    except ValueError as exc:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(exc),
        )
    return registro


@router.get(
    "/extrato",
    response_model=ExtratoDiaOut,
    summary="Extrato de marcacoes do dia atual",
)
def obter_extrato_dia(
    *,
    db: DbSession,
    current_user: CurrentUser,
    dia: date | None = None,
) -> ExtratoDiaOut:
    """
    Retorna as marcacoes do usuario logado em um dia específico (padrao: hoje).
    """
    if dia is None:
        dia = date.today()

    registros = get_registros_por_usuario_dia(db, current_user.id, dia)
    return ExtratoDiaOut(
        data=dia.isoformat(),
        total_marcacoes=len(registros),
        registros=registros,
    )


@router.get(
    "/resumo",
    response_model=MarcacaoResumoOut,
    summary="Resumo da ultima marcacao do usuario",
)
def obter_resumo_marcacao(
    *,
    db: DbSession,
    current_user: CurrentUser,
) -> MarcacaoResumoOut:
    """
    Retorna a ultima marcacao do usuario e o tipo esperado para a proxima marcacao.
    Util para exibir na tela principal de marcacao de ponto.
    """
    ultimo = get_ultimo_registro_por_usuario(db, current_user.id)

    proximo_tipo: str | None = None
    if ultimo is None:
        proximo_tipo = "entrada"
    else:
        proximo_tipo = TIPO_PROXIMO_ESPERADO.get(ultimo.tipo, "entrada")

    return MarcacaoResumoOut(
        ultima_marcacao=ultimo,
        proximo_tipo_esperado=proximo_tipo,
    )