package br.edu.crud.customer.dto;

import java.util.Locale;

public record CustomerRequestDto(Long id, String name, String email, String city, Integer age) {
    public CustomerRequestDto sanitize() {
        CustomerRequestDto sanitized = new CustomerRequestDto(
                id,
                normalize(name),
                email == null ? null : email.trim().toLowerCase(Locale.ROOT),
                normalize(city),
                age
        );
        sanitized.validate();
        return sanitized;
    }

    public CustomerRequestDto prepareForUpdate() {
        CustomerRequestDto sanitized = sanitize();
        if (sanitized.id() == null) {
            throw new IllegalArgumentException("Customer id is required for update");
        }
        return sanitized;
    }

    private void validate() {
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
                || city == null || city.isBlank()
                || age == null || age < 18 || age > 120) {
            throw new IllegalArgumentException("Invalid customer data");
        }
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
