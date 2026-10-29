"""
CRUD de RegistroPonto (S1.07, S1.08, S2.02).
"""
from datetime import date, datetime, timedelta, timezone
from typing import Optional

from sqlalchemy.orm import Session

from app.models.registro_ponto import RegistroPonto
from app.models.usuario import Usuario

# Sequencia valida das quatro marcacoes padrao (Portaria 671/2021 MTE):
# entrada -> saida_intervalo -> retorno_intervalo -> saida -> entrada (novo dia)
SEQUENCIA_VALIDA = {
    None: ["entrada"],
    "entrada": ["saida_intervalo"],
    "saida_intervalo": ["retorno_intervalo"],
    "retorno_intervalo": ["saida"],
    "saida": ["entrada"],
}


def criar_registro_ponto(
    db: Session,
    usuario: Usuario,
    tipo: str,
    ip_origem: str | None = None,
) -> RegistroPonto:
    """
    Cria uma nova marcação de ponto.
    O timestamp e gerado exclusivamente pelo servidor.
    Valida a sequencia de marcacoes antes de gravar (S2.02).

    A consulta de registros do dia usa SELECT ... FOR UPDATE para bloquear
    as linhas do usuario no dia, impedindo race conditions entre requisicoes
    concorrentes (duas marcacoes do mesmo tipo no mesmo dia).
    """
    # Bloqueia os registros do usuario para o dia corrente dentro desta
    # transacao. Qualquer concorrente que tentar criar um registro para o
    # mesmo usuario e mesmo dia aguarda ate esta transacao commit/rollback.
    hoje = date.today()
    inicio = datetime(hoje.year, hoje.month, hoje.day, tzinfo=timezone.utc)
    fim = inicio + timedelta(days=1)

    registros_hoje = (
        db.query(RegistroPonto)
        .filter(
            RegistroPonto.usuario_id == usuario.id,
            RegistroPonto.timestamp >= inicio,
            RegistroPonto.timestamp < fim,
        )
        .order_by(RegistroPonto.timestamp.asc())
        .with_for_update()
        .all()
    )

    # Ultimo registro do dia (se houver)
    ultimo_hoje = registros_hoje[-1] if registros_hoje else None
    tipo_anterior = ultimo_hoje.tipo if ultimo_hoje else None

    # Regra: primeira marcacao do dia deve ser entrada
    if tipo_anterior is None and tipo != "entrada":
        raise ValueError(
            "A primeira marcacao do dia deve ser entrada."
        )

    # Regra: apos cada tipo, somente o proximo tipo da sequencia e valido
    tipos_permitidos = SEQUENCIA_VALIDA.get(tipo_anterior, [])
    if tipo not in tipos_permitidos:
        if tipo_anterior is None:
            raise ValueError(
                "A primeira marcacao do dia deve ser entrada."
            )
        raise ValueError(
            f"Sequencia invalida. Após '{tipo_anterior}' "
            f"somente '{tipos_permitidos[0]}' e permitido."
        )

    db_registro = RegistroPonto(
        usuario_id=usuario.id,
        tipo=tipo,
        ip_origem=ip_origem,
        origem="web",
    )
    db.add(db_registro)
    db.commit()
    db.refresh(db_registro)
    return db_registro


def get_registros_por_usuario_dia(
    db: Session,
    usuario_id: int,
    dia: date,
) -> list[RegistroPonto]:
    """
    Retorna as marcações de um usuário em um dia específico.
    """
    inicio = datetime(dia.year, dia.month, dia.day, tzinfo=timezone.utc)
    fim = inicio + timedelta(days=1)
    return (
        db.query(RegistroPonto)
        .filter(
            RegistroPonto.usuario_id == usuario_id,
            RegistroPonto.timestamp >= inicio,
            RegistroPonto.timestamp < fim,
        )
        .order_by(RegistroPonto.timestamp.asc())
        .all()
    )


def get_ultimo_registro_por_usuario(
    db: Session,
    usuario_id: int,
) -> Optional[RegistroPonto]:
    """Retorna a última marcação do usuário."""
    return (
        db.query(RegistroPonto)
        .filter(RegistroPonto.usuario_id == usuario_id)
        .order_by(RegistroPonto.timestamp.desc())
        .first()
    )