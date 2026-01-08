<#
.SYNOPSIS
Script para criar usuário de teste no PostgreSQL.

.DESCRIPTION
Este script cria o usuário marcuser_test com a senha test123 e concede as permissões necessárias
no banco de dados marcweb_test.

.NOTES
Este script deve ser executado com privilégios de administrador.
#>

# Solicitar senha do superusuário postgres
$postgresPassword = Read-Host "Digite a senha do superusuário postgres" -AsSecureString
$bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($postgresPassword)
$plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)

# Configurar variável de ambiente para a senha
$env:PGPASSWORD = $plainPassword

try {
    # Verificar se o usuário já existe
    Write-Host "Verificando se o usuário marcuser_test já existe..."
    $userCheck = & psql -U postgres -h localhost -p 5432 -c "SELECT 1 FROM pg_roles WHERE rolname='marcuser_test';" 2>&1

    if ($userCheck -match "1") {
        Write-Host "O usuário marcuser_test já existe."
    } else {
        Write-Host "Criando usuário de teste marcuser_test..."

        # Executar o script SQL para criar o usuário de teste
        $result = & psql -U postgres -h localhost -p 5432 -f "$PSScriptRoot\create_test_user.sql" 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Host "Usuário de teste criado com sucesso!"
        } else {
            Write-Host "Erro ao criar usuário de teste:"
            Write-Host $result
        }
    }

    # Testar a conexão com o usuário criado
    Write-Host "Testando conexão com o usuário de teste..."
    $env:PGPASSWORD = "test123"
    $testResult = & psql -U marcuser_test -h localhost -p 5432 -d marcweb_test -c "SELECT current_user;" 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Conexão bem-sucedida! Usuário: $($testResult -match 'marcuser_test')"
    } else {
        Write-Host "Falha na conexão com o usuário de teste:"
        Write-Host $testResult
    }
} catch {
    Write-Host "Ocorreu um erro:"
    Write-Host $_.Exception.Message
} finally {
    # Limpar variável de ambiente
    Remove-Item Env:PGPASSWORD
}