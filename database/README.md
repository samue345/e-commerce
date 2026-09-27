# Dataset do experimento

O dataset é gerado localmente, sem dependência de dados externos.

## Subir o banco com Docker

Na raiz do projeto:

```bash
docker compose up -d postgres
docker compose ps
```

O PostgreSQL será criado com:

```text
host: localhost
port: 5432
banco: clientes_db
usuario: clientes_user
senha: clientes_password
```

A aplicação possui duas instâncias Java (`app-1` e `app-2`) e o Nginx distribui
as requisições entre elas. As duas instâncias são stateless e compartilham o
mesmo banco PostgreSQL.

A migration `database/migrations/V1__create_clientes.sql` é executada
automaticamente na primeira criação do volume.

## Popular o banco

Com o container em execução, use o script de carga:

```bash
bash database/populate_database.sh 50000
```

Para preparar o cenário com 20 mil registros:

```bash
bash database/populate_database.sh 20000
```

O script limpa a tabela, reinicia o contador de IDs e importa o CSV usando o
`COPY` do PostgreSQL. Portanto, ele deve ser executado novamente entre cenários
que utilizem tamanhos diferentes de dataset.

Para executar novamente a migration do zero, somente se ainda não houver dados
importantes no banco:

```bash
docker compose down -v
docker compose up -d postgres
```

## Gerar os arquivos

No WSL, a partir da raiz do projeto:

```bash
python3 database/generate_dataset.py --rows 20000
python3 database/generate_dataset.py --rows 50000
```

Os arquivos serão criados em `database/data/`.

## Carregar no PostgreSQL

Depois de criar a tabela usando `database/schema.sql`, use o `COPY` do PostgreSQL:

```bash
psql "$DATABASE_URL" -f database/load_dataset.sql
```

Para o teste sem índice adicional, mantenha apenas o índice automático da chave
primária. Para o teste com índice, execute:

```sql
CREATE INDEX idx_clientes_cidade ON clientes (cidade);
```

A semente padrão torna os dados reproduzíveis entre execuções. Os registros
possuem somente as colunas necessárias para o CRUD e para a consulta por cidade.
