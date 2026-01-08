@echo off
cd /d %~dp0
echo Executando testes a partir de: %CD%
mvn test -Dspring.profiles.active=test
pause