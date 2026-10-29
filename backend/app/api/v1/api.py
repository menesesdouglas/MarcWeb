"""Agregação dos roteadores da API v1."""
from fastapi import APIRouter

from app.api.v1.endpoints import auth, users, ponto

api_router = APIRouter()

api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(users.router, prefix="/users", tags=["users"])
api_router.include_router(ponto.router, prefix="/ponto", tags=["ponto"])