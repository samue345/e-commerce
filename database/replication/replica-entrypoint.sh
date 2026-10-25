#!/usr/bin/env bash
set -euo pipefail

: "${PGDATA:=/var/lib/postgresql/data/pgdata}"
: "${PRIMARY_HOST:=postgres-primary}"
: "${REPLICATION_USER:=replicator}"
: "${REPLICATION_PASSWORD:=replicator_password}"

mkdir -p "$PGDATA"

if [[ ! -s "$PGDATA/PG_VERSION" ]]; then
  echo "Waiting for the primary PostgreSQL server..."
  until PGPASSWORD="$REPLICATION_PASSWORD" pg_isready \
    -h "$PRIMARY_HOST" \
    -U "$REPLICATION_USER" \
    -d postgres >/dev/null 2>&1; do
    sleep 2
  done

  echo "Creating the replica base backup..."
  rm -rf "$PGDATA"/*
  PGPASSWORD="$REPLICATION_PASSWORD" pg_basebackup \
    -h "$PRIMARY_HOST" \
    -D "$PGDATA" \
    -U "$REPLICATION_USER" \
    -Fp \
    -Xs \
    -P \
    -R
fi

exec "$@"
