package br.edu.crud;

import br.edu.crud.customer.controller.CustomerController;
import br.edu.crud.customer.dto.CustomerIdRequestDto;
import br.edu.crud.customer.dto.CustomerListRequestDto;
import br.edu.crud.customer.dto.CustomerRequestDto;
import br.edu.crud.customer.exception.CustomerNotFoundException;
import br.edu.crud.customer.mapper.CustomerMapper;
import br.edu.crud.customer.repository.CustomerRepository;
import br.edu.crud.customer.repository.JdbcCustomerRepository;
import br.edu.crud.customer.service.CustomerService;
import br.edu.crud.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

/** Composição central da aplicação: instancia e conecta todas as dependências. */
public final class AppConfig {
    private final int httpPort;
    private final HikariDataSource dataSource;
    private final CustomerController customerController;

    private AppConfig(int httpPort, HikariDataSource dataSource,
                      CustomerController customerController) {
        this.httpPort = httpPort;
        this.dataSource = dataSource;
        this.customerController = customerController;
    }

    public static AppConfig fromEnvironment() {
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnvironment();
        HikariDataSource dataSource = databaseConfig.createDataSource();
        CustomerRepository repository = new JdbcCustomerRepository(dataSource);
        CustomerService service = new CustomerService(repository);
        CustomerMapper mapper = new CustomerMapper();
        CustomerController controller = new CustomerController(service, mapper);

        int port = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", "8080"));
        return new AppConfig(port, dataSource, controller);
    }

    public Javalin createApplication() {
        return Javalin.create()
                .get("/health", ctx -> ctx.json(java.util.Map.of("status", "UP")))
                .get("/clientes/{id}", ctx -> ctx.json(customerController.findById(
                        new CustomerIdRequestDto(
                                Long.parseLong(ctx.pathParam("id"))))))
                .get("/clientes", ctx -> ctx.json(customerController.list(
                        CustomerListRequestDto.from(ctx))))
                .post("/clientes", ctx -> ctx.status(HttpStatus.CREATED).json(
                        customerController.create(ctx.bodyAsClass(
                                CustomerRequestDto.class))))
                .put("/clientes", ctx -> ctx.json(customerController.update(
                        ctx.bodyAsClass(CustomerRequestDto.class))))
                .delete("/clientes/{id}", ctx -> {
                    customerController.delete(new CustomerIdRequestDto(
                            Long.parseLong(ctx.pathParam("id"))));
                    ctx.status(HttpStatus.NO_CONTENT);
                })
                .exception(CustomerNotFoundException.class, (exception, ctx) ->
                        ctx.status(HttpStatus.NOT_FOUND).json(java.util.Map.of(
                                "error", exception.getMessage())))
                .exception(IllegalArgumentException.class, (exception, ctx) ->
                        ctx.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of(
                                "error", exception.getMessage())))
                .events(events -> events.serverStopped(dataSource::close));
    }

    public int httpPort() {
        return httpPort;
    }
}
