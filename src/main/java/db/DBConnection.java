package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/fraud_engine";
    private static final String USER = "postgres";
    private static final String PASSWORD = "jeet28";

    private static Connection connection = null;

    private DBConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL JDBC Driver not found.", e);
            }
        }
        return connection;
    }
}
