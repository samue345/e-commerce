#!/usr/bin/env bash
set -euo pipefail

PLAN="load-tests/fault-tolerance.jmx"
OUT_DIR="load-tests/results/fault-tolerance"
DURATION_SECONDS="${DURATION_SECONDS:-180}"
FAILURE_DELAY_SECONDS="${FAILURE_DELAY_SECONDS:-20}"
READERS="${READERS:-50}"
DATASET_SIZE="${DATASET_SIZE:-50000}"
RUN_NAME="database-failover-$(date +%Y%m%d-%H%M%S)"

mkdir -p "$OUT_DIR"

echo "Starting JMeter: $RUN_NAME"
jmeter -n -t "$PLAN" \
  -Jreaders="$READERS" \
  -JdatasetSize="$DATASET_SIZE" \
  -JdurationSeconds="$DURATION_SECONDS" \
  -l "$OUT_DIR/$RUN_NAME.jtl" \
  -e -o "$OUT_DIR/$RUN_NAME-report" \
  > "$OUT_DIR/$RUN_NAME.log" 2>&1 &
JMETER_PID=$!

sleep "$FAILURE_DELAY_SECONDS"
echo "Stopping the PostgreSQL primary..."
docker compose stop postgres-primary

echo "Promoting the replica..."
bash database/replication/promote_replica.sh

wait "$JMETER_PID"
echo "Database failover test finished: $OUT_DIR/$RUN_NAME-report"
echo "Rejoin the old primary after the test with:"
echo "bash database/replication/rejoin_primary.sh"
