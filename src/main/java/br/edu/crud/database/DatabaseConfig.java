package br.edu.crud.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;

public record DatabaseConfig(
        String jdbcUrl,
        String username,
        String password,
        int maximumPoolSize,
        int connectionRetries,
        int retryDelayMs
)
{
    public static DatabaseConfig fromEnvironment() {
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "5432");
        String name = env("DB_NAME", "clientes_db");

        return new DatabaseConfig(
                "jdbc:postgresql://%s:%s/%s".formatted(host, port, name),
                env("DB_USER", "clientes_user"),
                env("DB_PASSWORD", "clientes_password"),
                Integer.parseInt(env("DB_POOL_SIZE", "10")),
                Integer.parseInt(env("DB_CONNECTION_RETRIES", "5")),
                Integer.parseInt(env("DB_RETRY_DELAY_MS", "1000"))
        );
    }

    public HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(Math.min(2, maximumPoolSize));
        config.setConnectionTimeout(Math.max(1000, retryDelayMs));
        config.setValidationTimeout(Math.max(1000, retryDelayMs));
        config.setPoolName("clientes-pool");

        for (int attempt = 1; attempt <= connectionRetries; attempt++) {
            HikariDataSource dataSource = new HikariDataSource(config);
            try {
                dataSource.getConnection().close();
                return dataSource;
            } catch (SQLException exception) {
                dataSource.close();
                if (attempt == connectionRetries) {
                    throw new IllegalStateException(
                            "Unable to connect to the database after "
                                    + connectionRetries + " attempts", exception);
                }
                sleepBeforeRetry(attempt);
            }
        }

        throw new IllegalStateException("Database connection retry configuration is invalid");
    }

    private static String env(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep((long) retryDelayMs * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Database connection retry interrupted", exception);
        }
    }
}
