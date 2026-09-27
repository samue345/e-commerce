package br.edu.crud.customer.dto;

import java.util.Locale;

public record CustomerRequestDto(Long id, String name, String email, String city, Integer age) {
    public CustomerRequestDto sanitize() {
        return new CustomerRequestDto(
                id,
                normalize(name),
                email == null ? null : email.trim().toLowerCase(Locale.ROOT),
                normalize(city),
                age
        );
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
