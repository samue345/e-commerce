#!/usr/bin/env bash
set -euo pipefail

PLAN="load-tests/clientes-crud.jmx"
DURATION="${DURATION_SECONDS:-60}"
DATASET_SIZE="${DATASET_SIZE:-50000}"
RESULTS_DIR="load-tests/results"

mkdir -p "$RESULTS_DIR"

run_scenario() {
  local name="$1"
  local readers="$2"
  local writers="$3"

  echo "Running ${name}: ${readers} readers and ${writers} writers"
  jmeter -n \
    -t "$PLAN" \
    -Jreaders="$readers" \
    -Jwriters="$writers" \
    -JdatasetSize="$DATASET_SIZE" \
    -JdurationSeconds="$DURATION" \
    -l "$RESULTS_DIR/${name}.jtl" \
    -e \
    -o "$RESULTS_DIR/${name}-report"
}

run_scenario "scenario-a-50r-50w" 50 50
run_scenario "scenario-b-75r-25w" 75 25
run_scenario "scenario-c-25r-75w" 25 75
