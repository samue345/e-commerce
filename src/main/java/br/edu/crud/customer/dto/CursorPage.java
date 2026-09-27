package br.edu.crud.customer.dto;

import java.util.List;
import java.util.function.Function;

public record CursorPage<T>(List<T> items, Long nextCursor, boolean hasNext) {
    public static <T> CursorPage<T> from(List<T> fetchedItems, int limit,
                                         Function<T, Long> cursorExtractor) {
        boolean hasNext = fetchedItems.size() > limit;
        List<T> items = hasNext
                ? List.copyOf(fetchedItems.subList(0, limit))
                : List.copyOf(fetchedItems);
        Long nextCursor = hasNext && !items.isEmpty()
                ? cursorExtractor.apply(items.get(items.size() - 1))
                : null;
        return new CursorPage<>(items, nextCursor, hasNext);
    }
}
