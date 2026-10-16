#!/usr/bin/env bash
# Executa a bateria completa de experimentos do trabalho:
#
#   dataset (50k, 20k) x leitura/indice (id; email sem indice; email com indice)
#   x cenarios (A 50/50, B 75/25, C 25/75)
#
# Antes de CADA execucao o banco e recarregado com o dataset original, para
# que os registros inseridos pelos escritores de uma execucao nao afetem a
# proxima.
#
# Uso:  bash load-tests/run-experiments.sh
# Variaveis opcionais: DURATION_SECONDS (padrao 60), DATASETS ("50000 20000").
set -euo pipefail

# MSYS_NO_PATHCONV evita que o Git Bash (Windows) converta caminhos como /tmp
# nos comandos docker. Ele nao pode ser global: quebraria o launcher do jmeter.

PLAN="load-tests/clientes-crud.jmx"
DURATION="${DURATION_SECONDS:-60}"
DATASETS="${DATASETS:-50000 20000}"
OUT_DIR="load-tests/results/experiments"
SUMMARY="$OUT_DIR/runs.csv"
CONTAINER="crud-postgres"

mkdir -p "$OUT_DIR"
echo "run,dataset,read_mode,index,scenario,readers,writers,rows_before,rows_after" > "$SUMMARY"

psql() {
  MSYS_NO_PATHCONV=1 docker exec "$CONTAINER" psql -U clientes_user -d clientes_db -v ON_ERROR_STOP=1 -tAq "$@"
}

prepare_database() {
  local rows="$1" index="$2"
  psql -c "SET client_min_messages TO warning; DROP INDEX IF EXISTS idx_customers_email;"
  MSYS_NO_PATHCONV=1 bash database/populate_database.sh "$rows" >/dev/null
  if [[ "$index" == "email" ]]; then
    psql -c "CREATE INDEX idx_customers_email ON customers (email);"
  fi
  psql -c "VACUUM ANALYZE customers;"
}

run_jmeter() {
  local name="$1" rows="$2" mode="$3" readers="$4" writers="$5" duration="$6"
  rm -rf "$OUT_DIR/$name.jtl" "$OUT_DIR/$name-report"
  jmeter -n -t "$PLAN" \
    -Jreaders="$readers" -Jwriters="$writers" \
    -JdatasetSize="$rows" -JdurationSeconds="$duration" -JreadMode="$mode" \
    -l "$OUT_DIR/$name.jtl" -e -o "$OUT_DIR/$name-report" > "$OUT_DIR/$name.log" 2>&1
}

# Aquecimento: a JVM das instancias Java compila o codigo "quente" nos
# primeiros segundos; essa execucao e descartada.
echo "Aquecimento (descartado)..."
prepare_database 50000 none
run_jmeter warmup 50000 id 50 50 20
rm -rf "$OUT_DIR/warmup"*

for rows in $DATASETS; do
  for config in "id none" "email none" "email email"; do
    read -r mode index <<< "$config"
    for scenario in "A 50 50" "B 75 25" "C 25 75"; do
      read -r label readers writers <<< "$scenario"
      name="${rows}-${mode}-idx_${index}-${label}"

      echo "[$(date +%H:%M:%S)] $name: ${readers} leitores, ${writers} escritores"
      prepare_database "$rows" "$index"
      before="$(psql -c 'SELECT COUNT(*) FROM customers;')"
      run_jmeter "$name" "$rows" "$mode" "$readers" "$writers" "$DURATION"
      after="$(psql -c 'SELECT COUNT(*) FROM customers;')"

      echo "$name,$rows,$mode,$index,$label,$readers,$writers,$before,$after" >> "$SUMMARY"
    done
  done
done

echo "Restaurando o banco com 50000 registros e sem indice extra..."
prepare_database 50000 none
echo "Concluido. Resultados em $OUT_DIR"
