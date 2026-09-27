package br.edu.crud.customer;

import java.util.List;

public final class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer findById(long id) {
        return repository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public List<Customer> findByCity(String city, int limit) {
        return repository.findByCity(city, Math.min(Math.max(limit, 1), 100));
    }

    public Customer create(CustomerRequest request) {
        validate(request);
        return repository.create(request);
    }

    public Customer update(long id, CustomerRequest request) {
        validate(request);
        if (!repository.update(id, request)) throw new CustomerNotFoundException(id);
        return findById(id);
    }

    public void delete(long id) {
        if (!repository.delete(id)) throw new CustomerNotFoundException(id);
    }

    private static void validate(CustomerRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()
                || request.email() == null || request.email().isBlank()
                || request.city() == null || request.city().isBlank()
                || request.age() == null || request.age() < 18 || request.age() > 120) {
            throw new IllegalArgumentException("Invalid customer data");
        }
    }
}
