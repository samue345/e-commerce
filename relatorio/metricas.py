"""Le os resultados do JMeter (.jtl) e calcula as metricas de cada execucao.

Uso (a partir da raiz do projeto):
    python relatorio/metricas.py

Gera relatorio/metricas.csv, usado pelo gerador do relatorio.
"""

from __future__ import annotations

import csv
from collections import defaultdict
from pathlib import Path

EXPERIMENTS = Path("load-tests/results/experiments")
OUTPUT = Path("relatorio/metricas.csv")

LABELS = {
    "Read customer by id": "leitura",
    "Read customer by email": "leitura",
    "Create customer": "escrita",
}

FIELDS = ["run", "dataset", "read_mode", "index", "scenario", "readers", "writers",
          "rows_before", "rows_after", "operation", "requests", "duration_s",
          "throughput", "avg_ms", "p50_ms", "p90_ms", "p95_ms", "p99_ms", "max_ms",
          "errors"]


def percentile(sorted_values: list[int], p: float) -> int:
    index = max(0, min(len(sorted_values) - 1, round(p / 100 * len(sorted_values)) - 1))
    return sorted_values[index]


def summarize(jtl: Path) -> dict[str, dict]:
    elapsed: dict[str, list[int]] = defaultdict(list)
    errors: dict[str, int] = defaultdict(int)
    start, end = None, None

    with jtl.open(newline="") as file:
        for row in csv.DictReader(file):
            timestamp, duration = int(row["timeStamp"]), int(row["elapsed"])
            start = timestamp if start is None else min(start, timestamp)
            end = timestamp + duration if end is None else max(end, timestamp + duration)

            for operation in (LABELS[row["label"]], "total"):
                elapsed[operation].append(duration)
                if row["success"] != "true":
                    errors[operation] += 1

    seconds = (end - start) / 1000
    result = {}
    for operation, values in elapsed.items():
        values.sort()
        result[operation] = {
            "requests": len(values),
            "duration_s": round(seconds, 1),
            "throughput": round(len(values) / seconds, 1),
            "avg_ms": round(sum(values) / len(values), 1),
            "p50_ms": percentile(values, 50),
            "p90_ms": percentile(values, 90),
            "p95_ms": percentile(values, 95),
            "p99_ms": percentile(values, 99),
            "max_ms": values[-1],
            "errors": errors[operation],
        }
    return result


def main() -> None:
    with (EXPERIMENTS / "runs.csv").open(newline="") as file:
        runs = list(csv.DictReader(file))

    with OUTPUT.open("w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=FIELDS)
        writer.writeheader()
        for run in runs:
            for operation, stats in summarize(EXPERIMENTS / f"{run['run']}.jtl").items():
                writer.writerow({**run, "operation": operation, **stats})

    print(f"Metricas de {len(runs)} execucoes gravadas em {OUTPUT}")


if __name__ == "__main__":
    main()
