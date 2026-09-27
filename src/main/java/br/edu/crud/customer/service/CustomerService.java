package br.edu.crud.customer.service;

import br.edu.crud.customer.dto.CursorPage;
import br.edu.crud.customer.dto.CustomerListRequestDto;
import br.edu.crud.customer.dto.CustomerRequestDto;
import br.edu.crud.customer.exception.CustomerNotFoundException;
import br.edu.crud.customer.model.Customer;
import br.edu.crud.customer.repository.CustomerRepository;

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

    public CursorPage<Customer> cursorPage(CustomerListRequestDto request) {
        int safeLimit = Math.min(Math.max(request.limit(), 1), 100);
        long currentCursor = request.cursor() == null ? 0 : Math.max(request.cursor(), 0);
        List<Customer> customers = repository.findAfterId(request.city(), currentCursor, safeLimit + 1);
        boolean hasNext = customers.size() > safeLimit;
        if (hasNext) customers = customers.subList(0, safeLimit);
        Long nextCursor = hasNext && !customers.isEmpty()
                ? customers.get(customers.size() - 1).id()
                : null;
        return new CursorPage<>(customers, nextCursor, hasNext);
    }

    public Customer create(CustomerRequestDto request) {
        CustomerRequestDto sanitizedRequest = sanitizeAndValidate(request);
        return repository.create(sanitizedRequest);
    }

    public Customer update(CustomerRequestDto request) {
        CustomerRequestDto sanitizedRequest = sanitizeAndValidate(request);
        if (sanitizedRequest.id() == null) {
            throw new IllegalArgumentException("Customer id is required for update");
        }
        if (!repository.update(sanitizedRequest.id(), sanitizedRequest)) {
            throw new CustomerNotFoundException(sanitizedRequest.id());
        }
        return findById(sanitizedRequest.id());
    }

    public void delete(long id) {
        if (!repository.delete(id)) throw new CustomerNotFoundException(id);
    }

    private static CustomerRequestDto sanitizeAndValidate(CustomerRequestDto request) {
        CustomerRequestDto sanitized = request == null ? null : request.sanitize();
        if (sanitized == null || sanitized.name() == null || sanitized.name().isBlank()
                || sanitized.email() == null || sanitized.email().isBlank()
                || !sanitized.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
                || sanitized.city() == null || sanitized.city().isBlank()
                || sanitized.age() == null || sanitized.age() < 18 || sanitized.age() > 120) {
            throw new IllegalArgumentException("Invalid customer data");
        }
        return sanitized;
    }
}
