CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    cidade VARCHAR(80) NOT NULL,
    idade INTEGER NOT NULL CHECK (idade BETWEEN 18 AND 120),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índice opcional para o experimento de comparação.
-- Execute apenas na etapa "com índice adicional":
-- CREATE INDEX idx_clientes_cidade ON clientes (cidade);
