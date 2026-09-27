package br.edu.crud.customer.repository;

import br.edu.crud.customer.dto.CustomerRequestDto;
import br.edu.crud.customer.model.Customer;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CustomerRepository {
    private final DataSource dataSource;

    public CustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Customer> findById(long id) {
        String sql = "SELECT id, nome, email, cidade, idade, criado_em FROM clientes WHERE id = ?";
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Error finding customer", exception);
        }
    }

    public List<Customer> findByCity(String city, int limit) {
        String sql = "SELECT id, nome, email, cidade, idade, criado_em "
                + "FROM clientes WHERE cidade = ? ORDER BY id LIMIT ?";
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, city);
            statement.setInt(2, limit);
            try (ResultSet result = statement.executeQuery()) {
                List<Customer> customers = new ArrayList<>();
                while (result.next()) customers.add(mapRow(result));
                return customers;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Error listing customers", exception);
        }
    }

    public List<Customer> findAfterId(String city, long cursor, int limit) {
        String sql = "SELECT id, nome, email, cidade, idade, criado_em FROM clientes "
                + "WHERE id > ?" + (city == null || city.isBlank() ? "" : " AND cidade = ?")
                + " ORDER BY id LIMIT ?";
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameter = 1;
            statement.setLong(parameter++, cursor);
            if (city != null && !city.isBlank()) statement.setString(parameter++, city);
            statement.setInt(parameter, limit);
            try (ResultSet result = statement.executeQuery()) {
                List<Customer> customers = new ArrayList<>();
                while (result.next()) customers.add(mapRow(result));
                return customers;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Error finding customers after cursor", exception);
        }
    }

    public Customer create(CustomerRequestDto request) {
        String sql = "INSERT INTO clientes (nome, email, cidade, idade) VALUES (?, ?, ?, ?)";
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(statement, request);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new IllegalStateException("ID não gerado");
                return findById(keys.getLong(1)).orElseThrow();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Error creating customer", exception);
        }
    }

    public boolean update(long id, CustomerRequestDto request) {
        String sql = "UPDATE clientes SET nome = ?, email = ?, cidade = ?, idade = ? WHERE id = ?";
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, request);
            statement.setLong(5, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Error updating customer", exception);
        }
    }

    public boolean delete(long id) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM clientes WHERE id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Error deleting customer", exception);
        }
    }

    private static void setParameters(PreparedStatement statement, CustomerRequestDto request) throws SQLException {
        statement.setString(1, request.name());
        statement.setString(2, request.email());
        statement.setString(3, request.city());
        statement.setInt(4, request.age());
    }

    private static Customer mapRow(ResultSet result) throws SQLException {
        return new Customer(result.getLong("id"), result.getString("nome"),
                result.getString("email"), result.getString("cidade"),
                result.getInt("idade"), result.getTimestamp("criado_em").toInstant().toString());
    }
}
