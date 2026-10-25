#!/usr/bin/env bash
set -euo pipefail

PLAN="load-tests/fault-tolerance.jmx"
OUT_DIR="load-tests/results/fault-tolerance"
DURATION_SECONDS="${DURATION_SECONDS:-120}"
FAILURE_DELAY_SECONDS="${FAILURE_DELAY_SECONDS:-20}"
FAILURE_DURATION_SECONDS="${FAILURE_DURATION_SECONDS:-30}"
READERS="${READERS:-50}"
DATASET_SIZE="${DATASET_SIZE:-50000}"
APP_SERVICE="${APP_SERVICE:-app-1}"
RUN_NAME="web-${APP_SERVICE}-$(date +%Y%m%d-%H%M%S)"

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
echo "Stopping $APP_SERVICE..."
docker compose stop "$APP_SERVICE"

sleep "$FAILURE_DURATION_SECONDS"
echo "Starting $APP_SERVICE..."
docker compose start "$APP_SERVICE"

wait "$JMETER_PID"
echo "Web failure test finished: $OUT_DIR/$RUN_NAME-report"
