#!/usr/bin/env bash
set -euo pipefail

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  -c "CREATE ROLE $REPLICATION_USER WITH REPLICATION LOGIN PASSWORD '$REPLICATION_PASSWORD';"

cat >> "$PGDATA/pg_hba.conf" <<EOF
host replication $REPLICATION_USER 0.0.0.0/0 scram-sha-256
host replication $REPLICATION_USER ::0/0 scram-sha-256
EOF
