#!/usr/bin/env bash
set -euo pipefail

echo "Stopping the failed primary service..."
docker compose stop postgres-primary

echo "Promoting the replica..."
docker exec crud-postgres-replica pg_ctl \
  -D /var/lib/postgresql/data/pgdata \
  promote

echo "Reconnecting the Java applications to the promoted replica..."
DB_HOST=postgres-replica docker compose up -d --force-recreate app-1 app-2

echo "Replica promoted. The applications now use postgres-replica."
