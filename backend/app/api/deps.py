"""
Dependências da API: sessão de banco, usuário autenticado via JWT, controle de perfil (S1.04).
"""
from typing import Annotated, Generator

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError
from pydantic import ValidationError
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.database import SessionLocal
from app.core.security import decode_token
from app.models.usuario import Usuario
from app.schemas.token import TokenPayload

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/v1/auth/login")


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def get_current_user(
    db: Session = Depends(get_db),
    token: str = Depends(oauth2_scheme),
) -> Usuario:
    """
    Valida o JWT e retorna o usuário logado.
    Levanta 401 se o token for invalido ou o usuário nao existir/nao estiver ativo.
    """
    try:
        payload = decode_token(token)
        if payload is None:
            raise JWTError()
        token_data = TokenPayload(**payload)
    except (JWTError, ValidationError):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token invalido ou expirado",
            headers={"WWW-Authenticate": "Bearer"},
        )

    usuario = db.query(Usuario).filter(Usuario.id == int(token_data.sub)).first() if token_data.sub else None
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Usuario nao encontrado",
        )
    if usuario.status != "ativo":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Usuario inativo",
        )
    return usuario


def get_current_adm(user: Usuario = Depends(get_current_user)) -> Usuario:
    """Garante que o usuario logado tenha perfil ADM."""
    if user.perfil != "adm":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Acesso restrito a administradores",
        )
    return user


DbSession = Annotated[Session, Depends(get_db)]
CurrentUser = Annotated[Usuario, Depends(get_current_user)]
CurrentAdm = Annotated[Usuario, Depends(get_current_adm)]