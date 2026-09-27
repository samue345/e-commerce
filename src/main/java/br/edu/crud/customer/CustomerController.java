package br.edu.crud.customer;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

public final class CustomerController {
    
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    public void findById(Context context)
    {
        try {
            context.json(CustomerResponseDto.from(
                    service.findById(Long.parseLong(context.pathParam("id")))));
        }
        catch (CustomerNotFoundException exception)
        {
            context.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", exception.getMessage()));
        }
    }

    public void list(Context context) {
        String city = context.queryParam("cidade");
        String limitParam = context.queryParam("limit");
        int limit = Integer.parseInt(limitParam == null ? "20" : limitParam);
        String pageParam = context.queryParam("page");

        if (pageParam != null) {
            String sizeParam = context.queryParam("size");
            int size = Integer.parseInt(sizeParam == null ? "20" : sizeParam);
            context.json(toResponse(service.simplePage(city, Integer.parseInt(pageParam), size)));
            return;
        }

        String cursorParam = context.queryParam("cursor");
        Long cursor = cursorParam == null || cursorParam.isBlank()
                ? null : Long.parseLong(cursorParam);
        context.json(toResponse(service.cursorPage(city, cursor, limit)));
    }

    public void create(Context context)
    {
        try {
            context.status(HttpStatus.CREATED).json(CustomerResponseDto.from(
                    service.create(context.bodyAsClass(CustomerRequestDto.class))));
        }
        catch (IllegalArgumentException exception) {
            context.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", exception.getMessage()));
        }
    }

    public void update(Context context)
    {
        try {
            long id = Long.parseLong(context.pathParam("id"));
            context.json(CustomerResponseDto.from(
                    service.update(id, context.bodyAsClass(CustomerRequestDto.class))));
        }
        catch (CustomerNotFoundException exception) {
            context.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", exception.getMessage()));
        }
        catch (IllegalArgumentException exception) {
            context.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", exception.getMessage()));
        }
    }

    public void delete(Context context)
    {
        try {
            service.delete(Long.parseLong(context.pathParam("id")));
            context.status(HttpStatus.NO_CONTENT);
        }
        catch (CustomerNotFoundException exception) {
            context.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", exception.getMessage()));
        }
    }

    private static CursorPage<CustomerResponseDto> toResponse(CursorPage<Customer> page) {
        List<CustomerResponseDto> items = page.items().stream()
                .map(CustomerResponseDto::from)
                .toList();
        return new CursorPage<>(items, page.nextCursor(), page.hasNext());
    }

    private static SimplePage<CustomerResponseDto> toResponse(SimplePage<Customer> page) {
        List<CustomerResponseDto> items = page.items().stream()
                .map(CustomerResponseDto::from)
                .toList();
        return new SimplePage<>(items, page.page(), page.size(), page.hasNext());
    }
}
