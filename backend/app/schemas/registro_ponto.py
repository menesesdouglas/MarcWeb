"""
Schemas de marcação de ponto e extrato (S1.07, S1.08).
"""
from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, Field


class RegistroPontoCreate(BaseModel):
    """
    Apenas o tipo de marcação é enviado pelo frontend.
    O timestamp é gerado exclusivamente pelo servidor (README Seção 5, regra 1).
    """
    tipo: str = Field(..., pattern="^(entrada|saida)$")


class RegistroPontoOut(BaseModel):
    id: int
    tipo: str
    timestamp: datetime
    ip_origem: Optional[str] = None
    origem: str
    created_at: Optional[datetime] = None

    model_config = {"from_attributes": True}


class ExtratoDiaOut(BaseModel):
    """Extrato simples do dia atual do próprio usuário (S1.08)."""
    data: str
    total_marcacoes: int
    registros: List[RegistroPontoOut]


class MarcacaoResumoOut(BaseModel):
    """Resumo da última marcação para exibição na tela de ponto."""
    ultima_marcacao: Optional[RegistroPontoOut] = None
    proximo_tipo_esperado: Optional[str] = None  # 'entrada' ou 'saida'