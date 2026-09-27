package br.edu.crud.customer;

public record Customer(Long id, String name, String email, String city,
                       Integer age, String createdAt) {
}
