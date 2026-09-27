#!/usr/bin/env python3
"""Gera um dataset pequeno e reproduzível de clientes para o experimento.

Uso:
    python3 database/generate_dataset.py --rows 50000
    python3 database/generate_dataset.py --rows 20000 --output-dir database/data

O script não depende de bibliotecas externas. Ele gera um CSV que pode ser
carregado rapidamente no PostgreSQL usando COPY.
"""

from __future__ import annotations

import argparse
import csv
import random
from pathlib import Path


NOMES = [
    "Ana", "Beatriz", "Bruno", "Camila", "Carlos", "Daniel", "Eduardo",
    "Fernanda", "Gabriel", "Helena", "Isabela", "Joao", "Julia", "Lucas",
    "Mariana", "Marcos", "Mateus", "Patricia", "Paulo", "Rafael", "Renata",
    "Roberto", "Sofia", "Tiago", "Vinicius",
]

SOBRENOMES = [
    "Almeida", "Barbosa", "Cardoso", "Carvalho", "Costa", "Dias", "Ferreira",
    "Gomes", "Lima", "Martins", "Mendes", "Monteiro", "Moreira", "Oliveira",
    "Pereira", "Ramos", "Ribeiro", "Rocha", "Santos", "Silva", "Souza",
    "Teixeira", "Vieira",
]

CIDADES = [
    "Sao Paulo", "Rio de Janeiro", "Belo Horizonte", "Curitiba", "Salvador",
    "Brasilia", "Fortaleza", "Recife", "Porto Alegre", "Manaus",
]


def criar_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Gera clientes para PostgreSQL")
    parser.add_argument("--rows", type=int, default=50_000,
                        help="quantidade de registros a gerar (padrao: 50000)")
    parser.add_argument("--seed", type=int, default=20260930,
                        help="semente para tornar a massa reproduzivel")
    parser.add_argument("--output-dir", type=Path, default=Path("database/data"),
                        help="diretorio de saida")
    return parser


def gerar_csv(total: int, seed: int, caminho: Path) -> None:
    rng = random.Random(seed)
    caminho.parent.mkdir(parents=True, exist_ok=True)

    with caminho.open("w", newline="", encoding="utf-8") as arquivo:
        escritor = csv.writer(arquivo)
        escritor.writerow(["nome", "email", "cidade", "idade"])

        for numero in range(1, total + 1):
            nome = f"{rng.choice(NOMES)} {rng.choice(SOBRENOMES)}"
            email = f"cliente{numero}@exemplo.test"
            cidade = rng.choice(CIDADES)
            idade = rng.randint(18, 80)
            escritor.writerow([nome, email, cidade, idade])


def main() -> None:
    args = criar_parser().parse_args()
    if args.rows <= 0:
        raise SystemExit("--rows deve ser maior que zero")

    arquivo_csv = args.output_dir / f"clientes_{args.rows}.csv"
    gerar_csv(args.rows, args.seed, arquivo_csv)

    print(f"Dataset criado: {arquivo_csv}")
    print(f"Registros: {args.rows}")
    print(f"Semente: {args.seed}")


if __name__ == "__main__":
    main()
