# Validações de Endpoints - MarcWeb

## Introdução

Este documento contém recomendações de validações para os endpoints existentes no sistema MarcWeb. As validações são essenciais para:

- Garantir a integridade dos dados
- Prevenir ataques de injeção e entradas maliciosas
- Melhorar a experiência do usuário com mensagens de erro claras
- Manter a consistência dos dados no banco

As validações são baseadas nas melhores práticas do Jakarta Validation (Bean Validation) e incluem validações de entrada, formato e negócio.

## Tipos de Validação Recomendados

### Validações de Entrada
- `@NotNull`: Campo obrigatório (não nulo)
- `@NotBlank`: Campo obrigatório e não vazio (para strings)
- `@NotEmpty`: Campo obrigatório e não vazio (para coleções)

### Validações de Formato
- `@Email`: Formato de email válido
- `@Pattern`: Padrão regex personalizado
- `@Size`: Tamanho mínimo/máximo

### Validações de Valor
- `@Min` / `@Max`: Valores numéricos mínimo/máximo
- `@Positive`: Valor positivo (> 0)
- `@Future` / `@Past`: Datas futuras/passadas

### Validações de Negócio
- Validações customizadas no DTO (ex: dataInicio <= dataFim)

## Validações por Controller

### AuthenticationController (`/api/auth`)

#### POST /api/auth/login
- **Método**: POST
- **Headers**:
  - `Content-Type: application/json`
- **Body** (JSON):
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Parâmetros**:
  - Body: `LoginRequest` (username, password)
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `username`: `@NotBlank(message = "Nome de usuário é obrigatório")` `@Size(min=3, max=50)`
  - `password`: `@NotBlank(message = "Senha é obrigatória")` `@Size(min=6)`

#### POST /api/auth/register
- **Método**: POST
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}` (requer ADMIN)
- **Body** (JSON):
  ```json
  {
    "username": "string",
    "nome": "string",
    "password": "string",
    "role": "ADMIN|USER",
    "consentGeoloc": "boolean"
  }
  ```
- **Parâmetros**:
  - Body: `@Valid CreateUser`
- **Validações atuais**: `@NotBlank` para username, nome, password; `@NotNull` para role
- **Validações sugeridas**: Adequadas, adicionar `@Size(min=6, max=100)` para password, `@Size(min=3, max=50)` para username

### RegistroPontoController (`/api/ponto`)

#### POST /api/ponto
- **Método**: POST
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`
- **Body** (JSON):
  ```json
  {
    "tipo": "ENTRADA|INICIO_DESCANSO|FIM_DESCANSO|SAIDA",
    "latitude": "number (opcional)",
    "longitude": "number (opcional)",
    "precisao": "number (opcional)",
    "justificativa": "string (opcional)"
  }
  ```
- **Parâmetros**:
  - Body: `@Valid CreateRegistroPonto`
- **Validações atuais**: `@NotNull` para tipo
- **Validações sugeridas**:
  - `tipo`: Já validado
  - `latitude`: `@DecimalMin(-90.0) @DecimalMax(90.0)` se fornecido
  - `longitude`: `@DecimalMin(-180.0) @DecimalMax(180.0)` se fornecido
  - `precisao`: `@Positive` se fornecido

#### GET /api/ponto/me?start={start}&end={end}
- **Método**: GET
- **Headers**:
  - `Authorization: Bearer {token}`
- **Query Parameters**:
  - `start`: `LocalDateTime` (obrigatório)
  - `end`: `LocalDateTime` (obrigatório)
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `start`: `@NotNull(message = "Data de início é obrigatória")`
  - `end`: `@NotNull(message = "Data de fim é obrigatória")`
  - Validação de negócio: `start.isBefore(end)` com mensagem personalizada

### FolgaController (`/api/folgas`)

#### GET /api/folgas/{id}
- **Método**: GET
- **Headers**:
  - `Authorization: Bearer {token}` (requer ADMIN ou proprietário)
- **Path Parameters**:
  - `id`: `Long` (obrigatório)
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `id`: `@Positive(message = "ID deve ser positivo")`

#### GET /api/folgas/user/{userId}
- **Método**: GET
- **Headers**:
  - `Authorization: Bearer {token}` (requer ADMIN ou próprio usuário)
- **Path Parameters**:
  - `userId`: `Long` (obrigatório)
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `userId`: `@Positive(message = "ID do usuário deve ser positivo")`

#### GET /api/folgas
- **Método**: GET
- **Headers**:
  - `Authorization: Bearer {token}` (requer ADMIN)
- **Parâmetros**: Nenhum
- **Validações**: Não aplicável

#### POST /api/folgas
- **Método**: POST
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}` (requer ADMIN)
- **Body** (JSON):
  ```json
  {
    "userId": "number",
    "dataInicio": "2024-01-15",
    "dataFim": "2024-01-16",
    "tipo": "FERIAS|ATESTADO|FALTA_JUSTIFICADA",
    "justificativa": "string"
  }
  ```
- **Parâmetros**:
  - Body: `@Valid CreateFolga`
- **Validações atuais**: Completas (datas futuras, justificativa, etc.)
- **Validações sugeridas**: Adequadas

#### PUT /api/folgas/{id}
- **Método**: PUT
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}` (requer ADMIN)
- **Path Parameters**:
  - `id`: `Long` (obrigatório)
- **Body** (JSON):
  ```json
  {
    "dataInicio": "2024-01-15",
    "dataFim": "2024-01-16",
    "tipo": "FERIAS|ATESTADO|FALTA_JUSTIFICADA",
    "justificativa": "string"
  }
  ```
- **Parâmetros**:
  - Path: `id`
  - Body: `@Valid UpdateFolga`
- **Validações atuais**: Id sem validação, body validado
- **Validações sugeridas**: `id`: `@Positive`

#### DELETE /api/folgas/{id}
- **Método**: DELETE
- **Headers**:
  - `Authorization: Bearer {token}` (requer ADMIN)
- **Path Parameters**:
  - `id`: `Long` (obrigatório)
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `id`: `@Positive`

### UserController (`/api/user`)

#### GET /
- **Parâmetros**: Nenhum
- **Validações**: Não aplicável

#### GET /?pageNumber={pageNumber}
- **Parâmetros**: `@RequestParam int pageNumber`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `pageNumber`: `@Min(0, message = "Número da página deve ser não negativo")`

#### POST /
- **Parâmetros**: `@RequestBody @Valid CreateUser`
- **Validações atuais**: `@NotBlank` para campos string, `@NotNull` para role
- **Validações sugeridas**: Adequadas

#### PUT /{id}
- **Parâmetros**: `@PathVariable Long id`, `@RequestBody CreateUser` (FALTA @Valid!)
- **Validações atuais**: Id sem validação, body sem @Valid
- **Validações sugeridas**:
  - `id`: `@Positive`
  - Adicionar `@Valid` ao body

#### DELETE /{id}
- **Parâmetros**: `@PathVariable Long id`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `id`: `@Positive`

### ConfigEmpresaController (`/api/config-empresa`)

#### PUT /
- **Parâmetros**: `@RequestBody @Valid DataConfigEmpresa`
- **Validações atuais**: `@NotBlank` para nomeEmpresa, `@NotNull` para coordenadas e raio
- **Validações sugeridas**:
  - `latCentro`/`longCentro`: Validar intervalos geográficos (-90 a 90, -180 a 180)
  - `raioGeoloc`: `@Positive`

#### GET /
- **Parâmetros**: Nenhum
- **Validações**: Não aplicável

### CacheController

#### DELETE /invalidate
- **Parâmetros**: `@RequestParam String cacheName`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `cacheName`: `@NotBlank`

#### DELETE /invalidate/key
- **Parâmetros**: `@RequestParam String cacheName`, `@RequestParam String key`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `cacheName`: `@NotBlank`
  - `key`: `@NotBlank`

#### DELETE /invalidate/folha-ponto
- **Parâmetros**: `@RequestParam Long userId`, `@RequestParam int mes`, `@RequestParam int ano`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `userId`: `@Positive`
  - `mes`: `@Min(1) @Max(12)`
  - `ano`: `@Min(2000) @Max(2100)`

### ConformidadeController

#### GET /validar-folha
- **Parâmetros**: `@RequestParam Long userId`, `@RequestParam int mes`, `@RequestParam int ano`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `userId`: `@Positive`
  - `mes`: `@Min(1) @Max(12)`
  - `ano`: `@Min(2000) @Max(2100)`

#### POST /gerar-e-assinar
- **Parâmetros**: `@RequestParam Long userId`, `@RequestParam int mes`, `@RequestParam int ano`, `@RequestParam String chavePrivada`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `userId`: `@Positive`
  - `mes`: `@Min(1) @Max(12)`
  - `ano`: `@Min(2000) @Max(2100)`
  - `chavePrivada`: `@NotBlank`

#### POST /validar-assinatura
- **Parâmetros**: `@RequestParam String aejJson`, `@RequestParam String chavePublica`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `aejJson`: `@NotBlank`
  - `chavePublica`: `@NotBlank`

#### GET /validar-portaria-671
- **Parâmetros**: `@RequestParam Long userId`, `@RequestParam int mes`, `@RequestParam int ano`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: Mesmo que validar-folha

### FeriadoController

#### POST /sync
- **Parâmetros**: `@RequestParam int ano`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `ano`: `@Min(2000) @Max(2100)`

#### GET /buscar
- **Parâmetros**: `@RequestParam LocalDate start`, `@RequestParam LocalDate end`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `start`: `@NotNull`
  - `end`: `@NotNull`
  - Validação de negócio: start <= end

#### POST /cadastro
- **Parâmetros**: `@RequestBody @Valid CreateFeriado`
- **Validações atuais**: Presumivelmente adequadas (não analisado)
- **Validações sugeridas**: Verificar DTO

#### DELETE /{id}
- **Parâmetros**: `@PathVariable Long id`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `id`: `@Positive`

### FolhaPontoController

#### GET /gerar
- **Parâmetros**: `@RequestParam Long userId`, `@RequestParam int mes`, `@RequestParam int ano`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `userId`: `@Positive`
  - `mes`: `@Min(1) @Max(12)`
  - `ano`: `@Min(2000) @Max(2100)`

#### GET /me
- **Parâmetros**: `@RequestParam int mes`, `@RequestParam int ano`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `mes`: `@Min(1) @Max(12)`
  - `ano`: `@Min(2000) @Max(2100)`

### LogAuditoriaController

#### GET /buscar-por-usuario
- **Parâmetros**: `@RequestParam Long userId`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `userId`: `@Positive`

#### GET /buscar-por-periodo
- **Parâmetros**: `@RequestParam LocalDateTime inicio`, `@RequestParam LocalDateTime fim`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `inicio`: `@NotNull`
  - `fim`: `@NotNull`
  - Validação de negócio: inicio < fim

#### GET /buscar-por-categoria
- **Parâmetros**: `@RequestParam CategoriaLog categoria`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `categoria`: `@NotNull`

#### GET /buscar-por-status
- **Parâmetros**: `@RequestParam StatusLog status`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `status`: `@NotNull`

### NotificacaoController

#### GET /minhas
- **Parâmetros**: Nenhum
- **Validações**: Não aplicável

#### POST /marcar-lida
- **Parâmetros**: `@RequestParam Long notificacaoId`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**: `notificacaoId`: `@Positive`

#### POST /enviar
- **Parâmetros**: `@RequestParam Long userId`, `@RequestParam String titulo`, `@RequestParam String mensagem`, `@RequestParam String tipo`, `@RequestParam String link`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `userId`: `@Positive`
  - `titulo`: `@NotBlank @Size(max=255)`
  - `mensagem`: `@NotBlank @Size(max=1000)`
  - `tipo`: `@NotBlank`
  - `link`: `@NotBlank` (se obrigatório)

### RelatorioController

#### POST /gerar-pdf
- **Parâmetros**: `@RequestParam LocalDateTime start`, `@RequestParam LocalDateTime end`, `@RequestParam Long userId`
- **Validações atuais**: Nenhuma
- **Validações sugeridas**:
  - `start`: `@NotNull`
  - `end`: `@NotNull`
  - `userId`: `@Positive`
  - Validação de negócio: start < end

### Outros Controllers (AEJ, AssinaturaDigital, Health, Performance)

Para os controllers restantes, aplicar validações similares baseadas nos padrões acima:
- IDs: `@Positive`
- Strings obrigatórias: `@NotBlank`
- Datas: `@NotNull` e validações de período
- Números: `@Min/@Max` conforme contexto

## Recomendações Gerais

1. **Parâmetros de Path**: Sempre validar IDs com `@Positive`
2. **Parâmetros de Query**: Validar presença e formato
3. **DTOs**: Garantir `@Valid` nos bodies
4. **Datas**: Validar períodos lógicos (início <= fim)
5. **Enums**: Usar `@NotNull` para enums obrigatórios
6. **Mensagens**: Personalizar mensagens de erro em português
7. **Testes**: Criar testes unitários para validações

## Priorização

- **Alta**: Endpoints de criação/modificação (POST/PUT)
- **Média**: Endpoints de consulta com parâmetros
- **Baixa**: Endpoints simples de leitura sem parâmetros

Este documento serve como guia para implementação das validações, mantendo o código fonte inalterado conforme solicitado.
