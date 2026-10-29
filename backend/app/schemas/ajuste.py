"""
Schemas de ajuste de ponto (S2.03, S2.04).
"""
from datetime import date, datetime
from typing import Optional
from pydantic import BaseModel, Field


class AjusteCreate(BaseModel):
    """Dados para solicitar um ajuste de ponto (funcionario)."""

    tipo_solicitado: str = Field(
        ...,
        pattern="^(entrada|saida_intervalo|retorno_intervalo|saida)$",
    )
    data_solicitada: date
    justificativa: str = Field(..., min_length=10, max_length=1000)


class AjusteOut(BaseModel):
    id: int
    usuario_id: int
    tipo_solicitado: str
    data_solicitada: date
    justificativa: str
    status: str
    observacao_adm: Optional[str] = None
    criado_em: Optional[datetime] = None
    atualizado_em: Optional[datetime] = None

    model_config = {"from_attributes": True}


class AjusteStatusUpdate(BaseModel):
    """Payload para aprovar ou rejeitar um ajuste de ponto (apenas ADM)."""

    status: str
    observacao_adm: Optional[str] = None


class AjusteListOut(BaseModel):
    """Extrato resumido de ajustes do usuario (para listagem)."""

    id: int
    tipo_solicitado: str
    data_solicitada: date
    justificativa: str
    status: str
    observacao_adm: Optional[str] = None
    criado_em: Optional[datetime] = None

    model_config = {"from_attributes": True}