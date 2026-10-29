"""
CRUD de Usuario (S1.05).
Validação de unicidade de e-mail e matrícula.
"""
from sqlalchemy.orm import Session

from app.models.usuario import Usuario
from app.schemas.usuario import UsuarioCreate, UsuarioUpdate


def get_usuario_by_id(db: Session, usuario_id: int) -> Usuario | None:
    return db.query(Usuario).filter(Usuario.id == usuario_id).first()


def get_usuario_by_email(db: Session, email: str) -> Usuario | None:
    return db.query(Usuario).filter(Usuario.email == email).first()


def get_usuario_by_matricula(db: Session, matricula: str) -> Usuario | None:
    return db.query(Usuario).filter(Usuario.matricula == matricula).first()


def get_usuarios(db: Session, skip: int = 0, limit: int = 100) -> list[Usuario]:
    return db.query(Usuario).offset(skip).limit(limit).all()


def criar_usuario(db: Session, usuario_in: UsuarioCreate) -> Usuario:
    """
    Cria um novo usuário. O caller já deve ter verificado unicidade de e-mail/matrícula.
    """
    from app.core.security import hash_password

    db_usuario = Usuario(
        nome=usuario_in.nome,
        email=usuario_in.email,
        senha_hash=hash_password(usuario_in.senha),
        cpf=usuario_in.cpf,
        matricula=usuario_in.matricula,
        perfil=usuario_in.perfil,
        status=usuario_in.status,
    )
    db.add(db_usuario)
    db.commit()
    db.refresh(db_usuario)
    return db_usuario


def atualizar_usuario(db: Session, db_usuario: Usuario, usuario_in: UsuarioUpdate) -> Usuario:
    update_data = usuario_in.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_usuario, field, value)
    db.add(db_usuario)
    db.commit()
    db.refresh(db_usuario)
    return db_usuario


def desativar_usuario(db: Session, db_usuario: Usuario) -> Usuario:
    db_usuario.status = "inativo"
    db.add(db_usuario)
    db.commit()
    db.refresh(db_usuario)
    return db_usuario