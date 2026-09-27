package br.edu.crud.customer.model;

public record Customer(
        Long id,
        String name,
        String email,
        String city,
        Integer age,
        String createdAt
)
{ }
