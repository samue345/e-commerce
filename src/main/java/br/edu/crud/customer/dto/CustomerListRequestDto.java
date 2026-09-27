package br.edu.crud.customer.dto;

import io.javalin.http.Context;

public record CustomerListRequestDto(String city, Long cursor, Integer limit) {
    public static CustomerListRequestDto from(Context context) {
        String city = context.queryParam("cidade");
        String cursorValue = context.queryParam("cursor");
        String limitValue = context.queryParam("limit");

        return new CustomerListRequestDto(
                city,
                cursorValue == null || cursorValue.isBlank() ? null : Long.parseLong(cursorValue),
                limitValue == null || limitValue.isBlank() ? 20 : Integer.parseInt(limitValue)
        );
    }

    public CustomerListRequestDto sanitize() {
        String sanitizedCity = city == null ? null : city.trim().replaceAll("\\s+", " ");
        long sanitizedCursor = cursor == null ? 0 : Math.max(cursor, 0);
        int sanitizedLimit = Math.min(Math.max(limit == null ? 20 : limit, 1), 100);
        return new CustomerListRequestDto(sanitizedCity, sanitizedCursor, sanitizedLimit);
    }

    public long cursorValue() {
        return cursor == null ? 0 : cursor;
    }

    public int fetchLimit() {
        return limit + 1;
    }
}
