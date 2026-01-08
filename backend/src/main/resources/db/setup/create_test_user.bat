@echo off
REM Script para criar usuário de teste no PostgreSQL
REM Este script deve ser executado com privilégios de administrador

REM Solicitar senha do superusuário postgres
set /p PGPASSWORD=Digite a senha do superusuário postgres:

REM Executar o script SQL para criar o usuário de teste
psql -U postgres -h localhost -p 5432 -f "%~dp0create_test_user.sql"

REM Verificar se o comando foi executado com sucesso
if %ERRORLEVEL% equ 0 (
    echo Usuário de teste criado com sucesso!
) else (
    echo Erro ao criar usuário de teste.
)

pause