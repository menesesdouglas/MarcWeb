"""
Aplicacao FastAPI principal.
Configura middlewares (CORS, log de requisicoes, tratamento global de excecoes)
e inclui os roteadores da API v1 (TR.03, TR.04, TR.05).
"""
import time
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from sqlalchemy.exc import SQLAlchemyError

from app.core.config import settings
from app.api.v1.api import api_router

app = FastAPI(
    title="REP-P MVP",
    description="Registrador Eletrônico de Ponto via Programa - MVP",
    version="1.0.0",
)

# -------------------------------------------------------------------------
# Middleware CORS (TR.05)
# -------------------------------------------------------------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.BACKEND_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# -------------------------------------------------------------------------
# Middleware de log de requisicoes (TR.03)
# -------------------------------------------------------------------------
@app.middleware("http")
async def log_requisicoes(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    process_time = (time.time() - start_time) * 1000
    print(
        f"{request.method} {request.url.path} - "
        f"status={response.status_code} - {process_time:.2f}ms"
    )
    return response


# -------------------------------------------------------------------------
# Tratamento global de excecoes (TR.04)
# -------------------------------------------------------------------------
@app.exception_handler(SQLAlchemyError)
async def sqlalchemy_exception_handler(request: Request, exc: SQLAlchemyError):
    return JSONResponse(
        status_code=500,
        content={"detail": "Erro interno do banco de dados"},
    )


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=500,
        content={"detail": "Erro interno do servidor"},
    )


# -------------------------------------------------------------------------
# Rotas da API
# -------------------------------------------------------------------------
app.include_router(api_router, prefix="/api/v1")


@app.get("/health", tags=["health"])
def health_check():
    return {"status": "ok"}