package br.edu.crud.customer;

import java.util.List;

public record SimplePage<T>(List<T> items, int page, int size, long total, int totalPages) {
}
