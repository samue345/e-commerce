package br.edu.crud.customer.controller;

import br.edu.crud.customer.dto.CursorPage;
import br.edu.crud.customer.dto.CustomerIdRequestDto;
import br.edu.crud.customer.dto.CustomerListRequestDto;
import br.edu.crud.customer.dto.CustomerRequestDto;
import br.edu.crud.customer.dto.CustomerResponseDto;
import br.edu.crud.customer.mapper.CustomerMapper;
import br.edu.crud.customer.model.Customer;
import br.edu.crud.customer.service.CustomerService;

public final class CustomerController
{
    private final CustomerService service;
    private final CustomerMapper mapper;

    public CustomerController(CustomerService service, CustomerMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public CustomerResponseDto findById(CustomerIdRequestDto request) {

        return mapper.toResponse(service.findById(request.id()));
    }

    public CursorPage<CustomerResponseDto> list(CustomerListRequestDto request)
    {
        return mapper.toResponse(service.cursorPage(request));
    }

    public CustomerResponseDto create(CustomerRequestDto request)
    {
        return mapper.toResponse(service.create(request));
    }

    public CustomerResponseDto update(CustomerRequestDto request) {
        return mapper.toResponse(service.update(request));
    }

    public void delete(CustomerIdRequestDto request) {
        service.delete(request.id());
    }

}
