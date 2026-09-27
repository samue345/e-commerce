package br.edu.crud.customer.repository;

import br.edu.crud.customer.dto.CustomerRequestDto;
import br.edu.crud.customer.dto.CustomerIdRequestDto;
import br.edu.crud.customer.dto.CustomerListRequestDto;
import br.edu.crud.customer.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    Optional<Customer> findById(CustomerIdRequestDto request);

    List<Customer> findAfter(CustomerListRequestDto request);

    Customer create(CustomerRequestDto request);

    boolean update(CustomerRequestDto request);

    boolean delete(CustomerIdRequestDto request);
}
