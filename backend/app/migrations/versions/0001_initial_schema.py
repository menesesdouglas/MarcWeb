"""initial schema

Revision ID: 0001_initial_schema
Revises:
Create Date: 2024-01-01 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = "0001_initial_schema"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "usuarios",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("nome", sa.String(length=200), nullable=False),
        sa.Column("email", sa.String(length=255), nullable=False),
        sa.Column("senha_hash", sa.String(length=255), nullable=False),
        sa.Column("cpf", sa.String(length=20), nullable=False),
        sa.Column("matricula", sa.String(length=50), nullable=False),
        sa.Column("perfil", sa.String(length=20), nullable=False),
        sa.Column("status", sa.String(length=20), nullable=False, server_default="ativo"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=True),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(op.f("ix_usuarios_email"), "usuarios", ["email"], unique=True)
    op.create_index(op.f("ix_usuarios_matricula"), "usuarios", ["matricula"], unique=True)
    op.create_index(op.f("ix_usuarios_id"), "usuarios", ["id"], unique=False)

    op.create_table(
        "registros_ponto",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("usuario_id", sa.Integer(), nullable=False),
        sa.Column("tipo", sa.String(length=30), nullable=False),
        sa.Column("timestamp", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("ip_origem", sa.String(length=45), nullable=True),
        sa.Column("origem", sa.String(length=20), nullable=False, server_default="web"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=True),
        sa.ForeignKeyConstraint(["usuario_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(op.f("ix_registros_ponto_id"), "registros_ponto", ["id"], unique=False)
    op.create_index(op.f("ix_registros_ponto_usuario_id"), "registros_ponto", ["usuario_id"], unique=False)
    op.create_index(
        "ix_registros_ponto_usuario_data",
        "registros_ponto",
        ["usuario_id", "timestamp"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index("ix_registros_ponto_usuario_data", table_name="registros_ponto")
    op.drop_index(op.f("ix_registros_ponto_usuario_id"), table_name="registros_ponto")
    op.drop_index(op.f("ix_registros_ponto_id"), table_name="registros_ponto")
    op.drop_table("registros_ponto")
    op.drop_index(op.f("ix_usuarios_matricula"), table_name="usuarios")
    op.drop_index(op.f("ix_usuarios_email"), table_name="usuarios")
    op.drop_index(op.f("ix_usuarios_id"), table_name="usuarios")
    op.drop_table("usuarios")