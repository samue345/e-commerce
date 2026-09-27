package br.edu.crud.customer.repository;

import br.edu.crud.customer.dto.CustomerRequestDto;
import br.edu.crud.customer.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    Optional<Customer> findById(long id);

    List<Customer> findAfterId(String city, long cursor, int limit);

    Customer create(CustomerRequestDto request);

    boolean update(long id, CustomerRequestDto request);

    boolean delete(long id);
}
