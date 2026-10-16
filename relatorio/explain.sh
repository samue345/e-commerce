#!/usr/bin/env bash
# Mostra como o PostgreSQL executa as consultas da API com e sem cada indice
# candidato (EXPLAIN ANALYZE). Os planos ficam em relatorio/explain/.
#
# Uso (com os containers rodando):  bash relatorio/explain.sh
set -euo pipefail

OUT="relatorio/explain"
CONTAINER="crud-postgres"
mkdir -p "$OUT"

psql() {
  MSYS_NO_PATHCONV=1 docker exec -i "$CONTAINER" psql -U clientes_user -d clientes_db -v ON_ERROR_STOP=1 -q "$@"
}

COLUMNS="id, name, email, city, age, created_at"
Q_ID="SELECT $COLUMNS FROM customers WHERE id = 12345"
Q_EMAIL="SELECT $COLUMNS FROM customers WHERE id > 0 AND email = 'cliente12345@exemplo.test' ORDER BY id LIMIT 2"
Q_CITY="SELECT $COLUMNS FROM customers WHERE id > 0 AND city = 'Recife' ORDER BY id LIMIT 21"

drop_indexes() {
  psql -c "SET client_min_messages TO warning;
           DROP INDEX IF EXISTS idx_customers_email;
           DROP INDEX IF EXISTS idx_customers_city;
           DROP INDEX IF EXISTS idx_customers_city_id;"
}

explain() {
  local name="$1" query="$2"
  # Executa 3 vezes antes para aquecer o cache; o plano registrado e o da 4a.
  for _ in 1 2 3; do psql -c "$query" >/dev/null; done
  psql -c "EXPLAIN (ANALYZE, BUFFERS) $query" > "$OUT/$name.txt"
  echo "  $name: $(grep 'Execution Time' "$OUT/$name.txt" | xargs)"
}

for rows in 50000 20000; do
  echo "Dataset de $rows registros"
  drop_indexes
  MSYS_NO_PATHCONV=1 bash database/populate_database.sh "$rows" >/dev/null
  psql -c "VACUUM ANALYZE customers;"

  explain "${rows}-id-sem_indice" "$Q_ID"
  explain "${rows}-email-sem_indice" "$Q_EMAIL"
  explain "${rows}-city-sem_indice" "$Q_CITY"

  psql -c "CREATE INDEX idx_customers_email ON customers (email); ANALYZE customers;"
  explain "${rows}-email-idx_email" "$Q_EMAIL"

  psql -c "CREATE INDEX idx_customers_city ON customers (city); ANALYZE customers;"
  explain "${rows}-city-idx_city" "$Q_CITY"

  psql -c "CREATE INDEX idx_customers_city_id ON customers (city, id); ANALYZE customers;"
  explain "${rows}-city-idx_city_id" "$Q_CITY"

  psql -At -c "SELECT relname || ',' || pg_relation_size(oid)
               FROM pg_class
               WHERE relname IN ('customers', 'customers_pkey', 'idx_customers_email',
                                 'idx_customers_city', 'idx_customers_city_id')
               ORDER BY relname;" > "$OUT/${rows}-tamanhos.csv"
done

echo "Restaurando o banco com 50000 registros e sem indice extra..."
drop_indexes
MSYS_NO_PATHCONV=1 bash database/populate_database.sh 50000 >/dev/null
psql -c "VACUUM ANALYZE customers;"
