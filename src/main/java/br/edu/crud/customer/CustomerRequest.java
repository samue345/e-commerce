package br.edu.crud.customer;

public record CustomerRequest(String name, String email, String city, Integer age) {
}
