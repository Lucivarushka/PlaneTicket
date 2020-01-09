package com.ticketoffice.repository;

import com.ticketoffice.model.Passenger;
import com.ticketoffice.util.ConnectionPool;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JavaJDBCPassRepositoryImpl implements PassengerRepository{
    private static final String PASS_TABLE = "passenger";
    private static final String INSERT = "insert into " + PASS_TABLE + " (name, surname, birthdate) VALUES(?, ?, ?)";
    private static final String UPDATE = "update " + PASS_TABLE + " set name = ?, surname = ?, birthdate = ? where id_passenger = ?";
    private static final String DELETE = "delete from " + PASS_TABLE + " where id_passenger = ?";
    private static final String SELECT_ALL = "select * from " + PASS_TABLE;
    private static final String SELECT_BY_ID = SELECT_ALL + " where id_passenger = ?";

    private Properties properties;
    private Connection connection;

    public JavaJDBCPassRepositoryImpl() {
        try {
            properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("liquibase/liquibase.properties"));
            connection = ConnectionPool.getInstanceConnection(properties).getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(Passenger passenger) throws Exception {
       setParameterPass(passenger, INSERT);
    }

    @Override
    public void update(Passenger passenger) throws Exception {
      updatePass(passenger,UPDATE);
    }

    @Override
    public void delete(Integer id) throws SQLException, ClassNotFoundException, InterruptedException {
        Connection connection = ConnectionPool.getInstanceConnection(properties).getConnection();
        try (PreparedStatement prepareStatement = connection.prepareStatement(DELETE)) {
            prepareStatement.setInt(1, id);
            prepareStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstanceConnection(properties).closeConnection(connection);
        }
    }

    @Override
    public List<Passenger> getAll() throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        Connection connection = ConnectionPool.getInstanceConnection(properties).getConnection();
        try (ResultSet resultSet = connection.createStatement()
                .executeQuery(SELECT_ALL)) {
            List<Passenger> accounts = new ArrayList<>();
            while (resultSet.next()) {
                accounts.add(createPassFromResult(resultSet));
            }
            return accounts;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            ConnectionPool.getInstanceConnection(properties).closeConnection(connection);
        }
    }

    @Override
    public Passenger getId(Integer id) throws Exception {
        Connection connection = ConnectionPool.getInstanceConnection(properties).getConnection();
        try (PreparedStatement prepareStatement = connection.prepareStatement(SELECT_BY_ID)) {
            prepareStatement.setInt(1, id);
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                if (resultSet.next()) {
                    return createPassFromResult(resultSet);
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            ConnectionPool.getInstanceConnection(properties).closeConnection(connection);
        }
    }

    private void setParameterPass(Passenger passenger, String query) throws Exception {
        Connection connection = ConnectionPool.getInstanceConnection(properties).getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, passenger.getName());
            preparedStatement.setString(2, passenger.getSurname());
            preparedStatement.setString(3, passenger.getBirthdate());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstanceConnection(properties).closeConnection(connection);
        }
    }

    private void updatePass(Passenger passenger, String query) throws Exception {
        Connection connection = ConnectionPool.getInstanceConnection(properties).getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, passenger.getName());
            preparedStatement.setString(2, passenger.getSurname());
            preparedStatement.setString(3, passenger.getBirthdate());
            preparedStatement.setInt(4,passenger.getIdPass());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstanceConnection(properties).closeConnection(connection);
        }
    }

    protected Passenger createPassFromResult(ResultSet resultSet) throws SQLException {
        Passenger passenger = new Passenger();
        passenger.setIdPass(resultSet.getInt("id_passenger"));
        passenger.setName(resultSet.getString("name"));
        passenger.setSurname(resultSet.getString("surname"));
        passenger.setBirthdate(resultSet.getString("birthdate"));
        return passenger;
    }
}