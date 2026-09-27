package br.edu.crud.customer.repository;

import br.edu.crud.customer.dto.CustomerRequestDto;
import br.edu.crud.customer.model.Customer;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcCustomerRepository implements CustomerRepository {
    private static final String CUSTOMER_COLUMNS =
            "id, nome, email, cidade, idade, criado_em";

    private final DataSource dataSource;

    public JdbcCustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Customer> findById(long id) {
        String sql = "SELECT " + CUSTOMER_COLUMNS
                + " FROM clientes WHERE id = ?";

        List<Customer> customers = queryCustomers(
                sql,
                statement -> statement.setLong(1, id)
        );

        return customers.stream().findFirst();
    }

    @Override
    public List<Customer> findAfterId(String city, long cursor, int limit) {
        String sql = buildCursorQuery(city);

        return queryCustomers(sql, statement -> {
            int parameter = 1;
            statement.setLong(parameter++, cursor);

            if (hasCityFilter(city)) {
                statement.setString(parameter++, city);
            }

            statement.setInt(parameter, limit);
        });
    }

    @Override
    public Customer create(CustomerRequestDto request) {
        String sql = "INSERT INTO clientes (nome, email, cidade, idade) "
                + "VALUES (?, ?, ?, ?) RETURNING id";

        long id = executeReturningId(sql, statement -> setCustomerParameters(statement, request));
        return findById(id).orElseThrow(() -> new IllegalStateException("Created customer was not found"));
    }

    @Override
    public boolean update(long id, CustomerRequestDto request) {
        String sql = "UPDATE clientes SET nome = ?, email = ?, cidade = ?, idade = ? "
                + "WHERE id = ?";

        int updatedRows = executeUpdate(sql, statement -> {
            setCustomerParameters(statement, request);
            statement.setLong(5, id);
        });

        return updatedRows == 1;
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM clientes WHERE id = ?";
        int deletedRows = executeUpdate(sql, statement -> statement.setLong(1, id));
        return deletedRows == 1;
    }

    private List<Customer> queryCustomers(String sql, StatementBinder binder) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);

            try (ResultSet result = statement.executeQuery()) {
                List<Customer> customers = new ArrayList<>();

                while (result.next()) {
                    customers.add(mapRow(result));
                }

                return customers;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Error querying customers", exception);
        }
    }

    private int executeUpdate(String sql, StatementBinder binder) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Error executing customer update", exception);
        }
    }

    private long executeReturningId(String sql, StatementBinder binder) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException("Created customer id was not returned");
                }

                return result.getLong(1);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Error creating customer", exception);
        }
    }

    private static String buildCursorQuery(String city) {
        String cityClause = hasCityFilter(city) ? " AND cidade = ?" : "";
        return "SELECT " + CUSTOMER_COLUMNS
                + " FROM clientes WHERE id > ?"
                + cityClause
                + " ORDER BY id LIMIT ?";
    }

    private static boolean hasCityFilter(String city) {
        return city != null && !city.isBlank();
    }

    private static void setCustomerParameters(
            PreparedStatement statement,
            CustomerRequestDto request
    ) throws SQLException {
        statement.setString(1, request.name());
        statement.setString(2, request.email());
        statement.setString(3, request.city());
        statement.setInt(4, request.age());
    }

    private static Customer mapRow(ResultSet result) throws SQLException {
        return new Customer(
                result.getLong("id"),
                result.getString("nome"),
                result.getString("email"),
                result.getString("cidade"),
                result.getInt("idade"),
                result.getTimestamp("criado_em").toInstant().toString()
        );
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
