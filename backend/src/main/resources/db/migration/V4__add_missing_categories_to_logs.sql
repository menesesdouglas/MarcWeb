-- Migration V4: Adiciona categorias faltantes na constraint de logs_auditoria
-- Resolve erro: "ERRO: a nova linha da relação "logs_auditoria" viola a restrição de verificação "logs_auditoria_categoria_check""

-- Primeiro, precisamos identificar a constraint existente
-- Vamos dropar a constraint antiga e criar uma nova com todas as categorias

-- Identifica e remove a constraint existente
ALTER TABLE logs_auditoria DROP CONSTRAINT IF EXISTS logs_auditoria_categoria_check;

-- Cria a nova constraint com todas as categorias do enum CategoriaLog
ALTER TABLE logs_auditoria 
ADD CONSTRAINT logs_auditoria_categoria_check 
CHECK (categoria IN ('PONTO', 'ADMIN', 'AUDITORIA', 'SISTEMA', 'RELATORIO', 'USO_SISTEMA'));

-- Comentário explicativo
COMMENT ON CONSTRAINT logs_auditoria_categoria_check ON logs_auditoria 
IS 'Constraint que valida as categorias de log de auditoria conforme enum CategoriaLog';