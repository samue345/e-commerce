package br.edu.crud.customer;

import java.util.Locale;

public record CustomerRequestDto(String name, String email, String city, Integer age) {
    public CustomerRequestDto sanitize() {
        return new CustomerRequestDto(
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
