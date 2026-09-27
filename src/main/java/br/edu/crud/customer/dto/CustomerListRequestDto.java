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
}
