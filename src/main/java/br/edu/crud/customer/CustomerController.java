package br.edu.crud.customer;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    public void findById(Context context) {
        try {
            context.json(service.findById(Long.parseLong(context.pathParam("id"))));
        } catch (CustomerNotFoundException exception) {
            context.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", exception.getMessage()));
        }
    }

    public void findByCity(Context context) {
        String city = context.queryParam("cidade");
        if (city == null || city.isBlank()) {
            context.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", "The cidade parameter is required"));
            return;
        }
        String limitParam = context.queryParam("limit");
        int limit = Integer.parseInt(limitParam == null ? "20" : limitParam);
        context.json(service.findByCity(city, limit));
    }

    public void create(Context context) {
        try {
            context.status(HttpStatus.CREATED).json(service.create(context.bodyAsClass(CustomerRequest.class)));
        } catch (IllegalArgumentException exception) {
            context.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", exception.getMessage()));
        }
    }

    public void update(Context context) {
        try {
            long id = Long.parseLong(context.pathParam("id"));
            context.json(service.update(id, context.bodyAsClass(CustomerRequest.class)));
        } catch (CustomerNotFoundException exception) {
            context.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            context.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", exception.getMessage()));
        }
    }

    public void delete(Context context) {
        try {
            service.delete(Long.parseLong(context.pathParam("id")));
            context.status(HttpStatus.NO_CONTENT);
        } catch (CustomerNotFoundException exception) {
            context.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", exception.getMessage()));
        }
    }
}
