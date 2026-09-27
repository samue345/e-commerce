package br.edu.crud.customer.service;

import br.edu.crud.customer.dto.CursorPage;
import br.edu.crud.customer.dto.CustomerListRequestDto;
import br.edu.crud.customer.dto.CustomerIdRequestDto;
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

    public Customer findById(CustomerIdRequestDto request) {
        return repository.findById(request)
                .orElseThrow(() -> new CustomerNotFoundException(request.id()));
    }

    public CursorPage<Customer> list(CustomerListRequestDto request)
    {
        CustomerListRequestDto query = request.sanitize();
        List<Customer> customers = repository.findAfter(query);
        return CursorPage.from(customers, query.limit(), Customer::id);
    }

    public Customer create(CustomerRequestDto request) {
        return repository.create(request.sanitize());
    }

    public Customer update(CustomerRequestDto request) {
        CustomerRequestDto updateRequest = request.prepareForUpdate();

        if (!repository.update(updateRequest)) {
            throw new CustomerNotFoundException(updateRequest.id());
        }

        return findById(new CustomerIdRequestDto(updateRequest.id()));
    }

    public void delete(CustomerIdRequestDto request)
    {
        if (!repository.delete(request)){
            throw new CustomerNotFoundException(request.id());
        }
    }

}
