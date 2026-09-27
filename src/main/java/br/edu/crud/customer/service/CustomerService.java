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

    public CursorPage<Customer> list(CustomerListRequestDto request)
    {
        CustomerListRequestDto query = request.sanitize();
        List<Customer> customers = repository.findAfterId(
                query.city(), query.cursorValue(), query.fetchLimit());
        return CursorPage.from(customers, query.limit(), Customer::id);
    }

    public Customer create(CustomerRequestDto request) {
        return repository.create(request.sanitize());
    }

    public Customer update(CustomerRequestDto request) {
        CustomerRequestDto updateRequest = request.prepareForUpdate();

        if (!repository.update(updateRequest.id(), updateRequest)) {
            throw new CustomerNotFoundException(updateRequest.id());
        }

        return findById(updateRequest.id());
    }

    public void delete(long id)
    {
        if (!repository.delete(id)){
            throw new CustomerNotFoundException(id);
        }
    }

}
