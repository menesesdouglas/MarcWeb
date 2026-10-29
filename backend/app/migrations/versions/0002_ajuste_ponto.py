"""ajuste_ponto

Revision ID: 0002_ajuste_ponto
Revises: 0001_initial_schema
Create Date: 2026-10-29 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = "0002_ajuste_ponto"
down_revision: Union[str, None] = "0001_initial_schema"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "ajustes_ponto",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("usuario_id", sa.Integer(), nullable=False),
        sa.Column("tipo_solicitado", sa.String(length=30), nullable=False),
        sa.Column("data_solicitada", sa.Date(), nullable=False),
        sa.Column("justificativa", sa.String(length=1000), nullable=False),
        sa.Column(
            "status", sa.String(length=20), nullable=False, server_default="pendente"
        ),
        sa.Column("observacao_adm", sa.String(length=1000), nullable=True),
        sa.Column(
            "criado_em",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=True,
        ),
        sa.Column(
            "atualizado_em",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=True,
        ),
        sa.ForeignKeyConstraint(
            ["usuario_id"], ["usuarios.id"], ondelete="CASCADE"
        ),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(
        op.f("ix_ajustes_ponto_id"), "ajustes_ponto", ["id"], unique=False
    )
    op.create_index(
        op.f("ix_ajustes_ponto_usuario_id"),
        "ajustes_ponto",
        ["usuario_id"],
        unique=False,
    )
    op.create_index(
        "ix_ajustes_ponto_usuario_status",
        "ajustes_ponto",
        ["usuario_id", "status"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index("ix_ajustes_ponto_usuario_status", table_name="ajustes_ponto")
    op.drop_index(op.f("ix_ajustes_ponto_usuario_id"), table_name="ajustes_ponto")
    op.drop_index(op.f("ix_ajustes_ponto_id"), table_name="ajustes_ponto")
    op.drop_table("ajustes_ponto")