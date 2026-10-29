"""
Endpoints de cadastro de usuarios (apenas ADM) (S1.05).
"""
from typing import Any

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.deps import DbSession, CurrentAdm
from app.crud.usuario import (
    criar_usuario,
    atualizar_usuario,
    desativar_usuario,
    get_usuario_by_email,
    get_usuario_by_matricula,
    get_usuario_by_id,
    get_usuarios,
)
from app.schemas.usuario import UsuarioCreate, UsuarioListOut, UsuarioOut, UsuarioUpdate

router = APIRouter()


@router.post(
    "/",
    response_model=UsuarioOut,
    status_code=status.HTTP_201_CREATED,
    summary="Cadastro de usuario (apenas ADM)",
)
def cadastrar_usuario(
    *,
    db: DbSession,
    current_adm: CurrentAdm,
    usuario_in: UsuarioCreate,
) -> Any:
    """
    Cadastra um novo funcionario ou ADM. Apenas usuarios com perfil ADM podem executar.
    Valida unicidade de email e matricula.
    """
    if get_usuario_by_email(db, usuario_in.email):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email ja cadastrado",
        )
    if get_usuario_by_matricula(db, usuario_in.matricula):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Matricula ja cadastrada",
        )

    usuario = criar_usuario(db, usuario_in)
    return usuario


@router.get(
    "/",
    response_model=list[UsuarioListOut],
    summary="Lista de usuarios (apenas ADM)",
)
def listar_usuarios(
    db: DbSession,
    current_adm: CurrentAdm,
    skip: int = 0,
    limit: int = 100,
) -> Any:
    """Lista todos os usuarios cadastrados."""
    return get_usuarios(db, skip=skip, limit=limit)


@router.get(
    "/{usuario_id}",
    response_model=UsuarioOut,
    summary="Detalhe de um usuario (apenas ADM)",
)
def obter_usuario(
    usuario_id: int,
    db: DbSession,
    current_adm: CurrentAdm,
) -> Any:
    """Retorna os dados de um usuario pelo ID."""
    usuario = get_usuario_by_id(db, usuario_id)
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario nao encontrado",
        )
    return usuario


@router.put(
    "/{usuario_id}",
    response_model=UsuarioOut,
    summary="Edita dados de um usuario (apenas ADM)",
)
def editar_usuario(
    usuario_id: int,
    db: DbSession,
    current_adm: CurrentAdm,
    usuario_in: UsuarioUpdate,
) -> Any:
    """Atualiza nome, email, cpf e/ou matricula de um usuario."""
    usuario = get_usuario_by_id(db, usuario_id)
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario nao encontrado",
        )
    # Se email/matricula forem alterados, garante unicidade.
    if usuario_in.email is not None and usuario_in.email != usuario.email:
        if get_usuario_by_email(db, usuario_in.email):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email ja cadastrado",
            )
    if usuario_in.matricula is not None and usuario_in.matricula != usuario.matricula:
        if get_usuario_by_matricula(db, usuario_in.matricula):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Matricula ja cadastrada",
            )
    return atualizar_usuario(db, usuario, usuario_in)


@router.patch(
    "/{usuario_id}/desativar",
    response_model=UsuarioOut,
    summary="Desativa um usuario (apenas ADM)",
)
def desativar_usuario_endpoint(
    usuario_id: int,
    db: DbSession,
    current_adm: CurrentAdm,
) -> Any:
    """Marca o usuario como inativo (nao exclui o registro)."""
    usuario = get_usuario_by_id(db, usuario_id)
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario nao encontrado",
        )
    return desativar_usuario(db, usuario)