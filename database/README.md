# Dataset do experimento

O dataset é gerado localmente, sem dependência de dados externos.

## Subir o banco com Docker

Na raiz do projeto:

```bash
docker compose up -d postgres-primary postgres-replica
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
as requisições entre elas. As duas instâncias são stateless e usam o
`postgres-primary`. O `postgres-replica` recebe as alterações por replicação
assíncrona de WAL.

A migration `database/migrations/V1__create_customers.sql` e o script de criação
do usuário de replicação são executados automaticamente na primeira criação do
volume do primary.

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

Para executar novamente a migration e preparar a replicação do zero, somente
se ainda não houver dados importantes no banco:

```bash
docker compose down -v
docker compose up -d postgres-primary postgres-replica
```

O comando `docker compose down -v` remove os volumes do PostgreSQL. Depois da
recriação, execute novamente o script de população.

## Gerar os arquivos

No WSL, a partir da raiz do projeto:

```bash
python3 database/generate_dataset.py --rows 20000
python3 database/generate_dataset.py --rows 50000
```

Os arquivos serão criados em `database/data/`.

## Carregar no PostgreSQL

O script `populate_database.sh` já cria o CSV, limpa a tabela e usa o `COPY` do
PostgreSQL:

```bash
bash database/populate_database.sh 50000
```

Para o teste sem índice adicional, mantenha apenas o índice automático da chave
primária. O índice abaixo é somente uma possibilidade para o experimento; ele
ainda não foi criado:

```sql
CREATE INDEX idx_customers_city ON customers (city);
```

A semente padrão torna os dados reproduzíveis entre execuções. Os registros
possuem somente as colunas necessárias para o CRUD e para a consulta por cidade.
