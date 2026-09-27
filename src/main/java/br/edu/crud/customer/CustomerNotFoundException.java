package br.edu.crud.customer;

public final class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(long id) {
        super("Customer not found: " + id);
    }
}
