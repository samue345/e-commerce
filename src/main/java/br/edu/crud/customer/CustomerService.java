package br.edu.crud.customer;

import java.util.List;

public final class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer findById(long id) {
        return repository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public List<Customer> findByCity(String city, int limit) {
        return repository.findByCity(city, Math.min(Math.max(limit, 1), 100));
    }

    public List<Customer> list(String city, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return city == null || city.isBlank()
                ? repository.findAll(safeLimit)
                : repository.findByCity(city, safeLimit);
    }

    public CursorPage<Customer> cursorPage(String city, Long cursor, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        long currentCursor = cursor == null ? 0 : Math.max(cursor, 0);
        List<Customer> customers = repository.findAfterId(city, currentCursor, safeLimit + 1);
        boolean hasNext = customers.size() > safeLimit;
        if (hasNext) customers = customers.subList(0, safeLimit);
        Long nextCursor = hasNext && !customers.isEmpty()
                ? customers.get(customers.size() - 1).id()
                : null;
        return new CursorPage<>(customers, nextCursor, hasNext);
    }

    public SimplePage<Customer> simplePage(String city, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        long total = repository.count(city);
        int totalPages = (int) Math.ceil((double) total / safeSize);
        int offset = (safePage - 1) * safeSize;
        List<Customer> customers = offset >= total
                ? List.of()
                : repository.findPage(city, offset, safeSize);
        return new SimplePage<>(customers, safePage, safeSize, total, totalPages);
    }

    public Customer create(CustomerRequest request) {
        validate(request);
        return repository.create(request);
    }

    public Customer update(long id, CustomerRequest request) {
        validate(request);
        if (!repository.update(id, request)) throw new CustomerNotFoundException(id);
        return findById(id);
    }

    public void delete(long id) {
        if (!repository.delete(id)) throw new CustomerNotFoundException(id);
    }

    private static void validate(CustomerRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()
                || request.email() == null || request.email().isBlank()
                || request.city() == null || request.city().isBlank()
                || request.age() == null || request.age() < 18 || request.age() > 120) {
            throw new IllegalArgumentException("Invalid customer data");
        }
    }
}
