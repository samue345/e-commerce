#!/usr/bin/env bash
set -euo pipefail

ROWS="${1:-50000}"
CONTAINER="${POSTGRES_CONTAINER:-crud-postgres}"
DATABASE="${POSTGRES_DB:-clientes_db}"
USER="${POSTGRES_USER:-clientes_user}"
CSV="database/data/clientes_${ROWS}.csv"

if [[ ! "$ROWS" =~ ^[1-9][0-9]*$ ]]; then
  echo "Uso: $0 [quantidade_de_registros]"
  exit 1
fi

if [[ ! -f "$CSV" ]]; then
  python3 database/generate_dataset.py --rows "$ROWS"
fi

echo "Aguardando o PostgreSQL ficar pronto..."
until docker exec "$CONTAINER" pg_isready -U "$USER" -d "$DATABASE" >/dev/null 2>&1; do
  sleep 2
done

docker cp "$CSV" "$CONTAINER:/tmp/clientes.csv"

docker exec "$CONTAINER" psql \
  -U "$USER" \
  -d "$DATABASE" \
  -v ON_ERROR_STOP=1 \
  -c "TRUNCATE TABLE clientes RESTART IDENTITY;" \
  -c "\\copy clientes (nome, email, cidade, idade) FROM '/tmp/clientes.csv' WITH (FORMAT csv, HEADER true);"

TOTAL="$(docker exec "$CONTAINER" psql -U "$USER" -d "$DATABASE" -tAc 'SELECT COUNT(*) FROM clientes;')"
echo "Banco populado com ${TOTAL//[[:space:]]/} clientes."
