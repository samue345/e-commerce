package br.edu.crud.customer;

public final class CustomerController {
    private final CustomerService service;
    private final CustomerMapper mapper;

    public CustomerController(CustomerService service, CustomerMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public CustomerResponseDto findById(long id) {

        return mapper.toResponse(service.findById(id));
    }

    public CursorPage<CustomerResponseDto> list(CustomerListRequestDto request)
    {
        return mapper.toResponse(service.cursorPage(request));
    }

    public CustomerResponseDto create(CustomerRequestDto request)
    {
        return mapper.toResponse(service.create(request));
    }

    public CustomerResponseDto update(long id, CustomerRequestDto request) {
        return mapper.toResponse(service.update(id, request));
    }

    public void delete(long id) {
        service.delete(id);
    }

}
