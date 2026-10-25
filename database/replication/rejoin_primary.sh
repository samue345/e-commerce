#!/usr/bin/env bash
set -euo pipefail

PRIMARY_CONTAINER="${PRIMARY_CONTAINER:-crud-postgres}"
REPLICA_CONTAINER="${REPLICA_CONTAINER:-crud-postgres-replica}"
REPLICATION_USER="${REPLICATION_USER:-replicator}"
REPLICATION_PASSWORD="${REPLICATION_PASSWORD:-replicator_password}"

if [[ "$(docker inspect -f '{{.State.Running}}' "$REPLICA_CONTAINER" 2>/dev/null || true)" != "true" ]]; then
  echo "The promoted replica must be running: $REPLICA_CONTAINER"
  exit 1
fi

PRIMARY_VOLUME="$(docker inspect -f '{{range .Mounts}}{{if eq .Destination "/var/lib/postgresql/data"}}{{.Name}}{{end}}{{end}}' "$PRIMARY_CONTAINER")"
NETWORK="$(docker inspect -f '{{range $name, $network := .NetworkSettings.Networks}}{{$name}}{{end}}' "$REPLICA_CONTAINER")"

if [[ -z "$PRIMARY_VOLUME" || -z "$NETWORK" ]]; then
  echo "Could not determine the primary volume or Docker network"
  exit 1
fi

echo "Stopping and removing the old primary container..."
docker compose stop postgres-primary >/dev/null || true
docker compose rm -f postgres-primary >/dev/null || true

echo "Recreating the old primary data volume..."
docker volume rm "$PRIMARY_VOLUME" >/dev/null
docker volume create "$PRIMARY_VOLUME" >/dev/null

echo "Copying the current primary into the new standby volume..."
docker run --rm \
  --user postgres \
  --network "$NETWORK" \
  --entrypoint sh \
  -e PGPASSWORD="$REPLICATION_PASSWORD" \
  -v "$PRIMARY_VOLUME:/var/lib/postgresql/data" \
  postgres:16-alpine \
  -ec "pg_basebackup -h postgres-replica -D /var/lib/postgresql/data -U $REPLICATION_USER -Fp -Xs -P -R && chmod 700 /var/lib/postgresql/data"

echo "Starting the old primary as a standby..."
docker compose up -d postgres-primary >/dev/null

until docker exec "$PRIMARY_CONTAINER" pg_isready -U clientes_user -d clientes_db >/dev/null 2>&1; do
  sleep 2
done

RECOVERY_STATE="$(docker exec "$PRIMARY_CONTAINER" psql -U clientes_user -d clientes_db -tAc 'SELECT pg_is_in_recovery();' | tr -d '[:space:]')"
if [[ "$RECOVERY_STATE" != "t" ]]; then
  echo "The rejoined node is not in standby mode"
  exit 1
fi

echo "The old primary was rejoined as a standby successfully."
