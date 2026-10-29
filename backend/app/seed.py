"""
Script de seed: cria o primeiro usuario ADM se nao existir.
Executa apos as migracoes Alembic na inicializacao do container.
"""
from app.core.config import settings
from app.core.database import SessionLocal, engine
from app.core.security import hash_password
from app.models.usuario import Usuario


def seed_adm():
    db = SessionLocal()
    try:
        existente = (
            db.query(Usuario)
            .filter(Usuario.email == settings.ADM_EMAIL)
            .first()
        )
        if existente:
            print(f"ADM ja existe: {settings.ADM_EMAIL}")
            return

        adm = Usuario(
            nome=settings.ADM_NOME,
            email=settings.ADM_EMAIL,
            senha_hash=hash_password(settings.ADM_PASSWORD),
            cpf=settings.ADM_CPF,
            matricula=settings.ADM_MATRICULA,
            perfil="adm",
            status="ativo",
        )
        db.add(adm)
        db.commit()
        print(f"ADM criado com sucesso: {settings.ADM_EMAIL}")
    finally:
        db.close()


if __name__ == "__main__":
    seed_adm()