package br.edu.crud.customer;

import java.util.List;

public final class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    public CustomerResponseDto findById(long id) {
        return CustomerResponseDto.from(service.findById(id));
    }

    public CursorPage<CustomerResponseDto> list(CustomerListRequestDto request) {
        return toResponse(service.cursorPage(request));
    }

    public CustomerResponseDto create(CustomerRequestDto request) {
        return CustomerResponseDto.from(service.create(request));
    }

    public CustomerResponseDto update(long id, CustomerRequestDto request) {
        return CustomerResponseDto.from(service.update(id, request));
    }

    public void delete(long id) {
        service.delete(id);
    }

    private static CursorPage<CustomerResponseDto> toResponse(CursorPage<Customer> page) {
        List<CustomerResponseDto> items = page.items().stream()
                .map(CustomerResponseDto::from)
                .toList();
        return new CursorPage<>(items, page.nextCursor(), page.hasNext());
    }
}
