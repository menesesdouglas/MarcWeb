"""
Endpoints de autenticacao: login e logout (S1.04).
"""
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from app.api.deps import DbSession
from app.core.security import create_access_token
from app.schemas.token import Token

router = APIRouter()


def authenticate_user(db: Session, email: str, senha: str):
    from app.crud.usuario import get_usuario_by_email
    from app.core.security import verify_password

    usuario = get_usuario_by_email(db, email)
    if not usuario:
        return None
    if not verify_password(senha, usuario.senha_hash):
        return None
    return usuario


@router.post("/login", response_model=Token, summary="Login de usuario")
def login(
    db: DbSession,
    form_data: OAuth2PasswordRequestForm = Depends(),
) -> Token:
    """
    Autentica o usuario com email e senha e retorna um token JWT de acesso.
    """
    usuario = authenticate_user(db, form_data.username, form_data.password)
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Email ou senha incorretos",
            headers={"WWW-Authenticate": "Bearer"},
        )
    if usuario.status != "ativo":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Usuario inativo",
        )

    access_token = create_access_token(subject=str(usuario.id))
    return Token(access_token=access_token, token_type="bearer")


@router.post("/logout", summary="Logout de usuario")
def logout() -> dict:
    """
    Logout: o cliente deve descartar o token. (Stateless JWT - invalido no lado do cliente.)
    """
    return {"detail": "Logout realizado com sucesso"}