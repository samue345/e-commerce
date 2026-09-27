package br.edu.crud.customer.dto;

import br.edu.crud.customer.model.Customer;

public record CustomerResponseDto(Long id, String name, String email, String city,
                                  Integer age, String createdAt) {
    public static CustomerResponseDto from(Customer customer) {
        return new CustomerResponseDto(customer.id(), customer.name(), customer.email(),
                customer.city(), customer.age(), customer.createdAt());
    }
}
