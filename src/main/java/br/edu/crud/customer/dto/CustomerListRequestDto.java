package br.edu.crud.customer.dto;

import io.javalin.http.Context;

public record CustomerListRequestDto(String city, String email, Long cursor, Integer limit) {
    public static CustomerListRequestDto from(Context context) {
        String city = context.queryParam("city");
        String email = context.queryParam("email");
        String cursorValue = context.queryParam("cursor");
        String limitValue = context.queryParam("limit");

        return new CustomerListRequestDto(
                city,
                email,
                cursorValue == null || cursorValue.isBlank() ? null : Long.parseLong(cursorValue),
                limitValue == null || limitValue.isBlank() ? 20 : Integer.parseInt(limitValue)
        );
    }

    public CustomerListRequestDto sanitize() {
        String sanitizedCity = city == null ? null : city.trim().replaceAll("\\s+", " ");
        String sanitizedEmail = email == null ? null : email.trim().toLowerCase();
        long sanitizedCursor = cursor == null ? 0 : Math.max(cursor, 0);
        int sanitizedLimit = Math.min(Math.max(limit == null ? 20 : limit, 1), 100);
        return new CustomerListRequestDto(sanitizedCity, sanitizedEmail, sanitizedCursor, sanitizedLimit);
    }

    public long cursorValue() {
        return cursor == null ? 0 : cursor;
    }

    public int fetchLimit() {
        return limit + 1;
    }
}
