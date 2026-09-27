package br.edu.crud.customer;

import java.util.List;

public final class CustomerMapper {
    public CustomerResponseDto toResponse(Customer customer) {
        return CustomerResponseDto.from(customer);
    }

    public CursorPage<CustomerResponseDto> toResponse(CursorPage<Customer> page) {
        List<CustomerResponseDto> items = page.items().stream()
                .map(this::toResponse)
                .toList();
        return new CursorPage<>(items, page.nextCursor(), page.hasNext());
    }
}
