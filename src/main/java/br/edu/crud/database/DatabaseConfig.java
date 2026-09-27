package br.edu.crud.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public record DatabaseConfig(String jdbcUrl, String username, String password,
                             int maximumPoolSize) {
    public static DatabaseConfig fromEnvironment() {
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "5432");
        String name = env("DB_NAME", "clientes_db");
        return new DatabaseConfig(
                "jdbc:postgresql://%s:%s/%s".formatted(host, port, name),
                env("DB_USER", "clientes_user"),
                env("DB_PASSWORD", "clientes_password"),
                Integer.parseInt(env("DB_POOL_SIZE", "10"))
        );
    }

    public HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(Math.min(2, maximumPoolSize));
        config.setPoolName("clientes-pool");
        return new HikariDataSource(config);
    }

    private static String env(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }
}
