package br.edu.crud;

import br.edu.crud.customer.CustomerController;
import br.edu.crud.customer.CustomerRepository;
import br.edu.crud.customer.CustomerService;
import br.edu.crud.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;

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
        CustomerRepository repository = new CustomerRepository(dataSource);
        CustomerService service = new CustomerService(repository);
        CustomerController controller = new CustomerController(service);

        int port = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", "8080"));
        return new AppConfig(port, dataSource, controller);
    }

    public Javalin createApplication() {
        return Javalin.create()
                .get("/health", ctx -> ctx.json(java.util.Map.of("status", "UP")))
                .get("/clientes/{id}", customerController::findById)
                .get("/clientes", customerController::list)
                .post("/clientes", customerController::create)
                .put("/clientes/{id}", customerController::update)
                .delete("/clientes/{id}", customerController::delete)
                .events(events -> events.serverStopped(dataSource::close));
    }

    public int httpPort() {
        return httpPort;
    }
}
