-- Execute dentro do PostgreSQL após criar a tabela.
-- Troque o nome do arquivo pelo dataset desejado.
\copy clientes (nome, email, cidade, idade) FROM 'database/data/clientes_50000.csv' WITH (FORMAT csv, HEADER true)
