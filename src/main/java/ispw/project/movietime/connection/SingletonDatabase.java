package ispw.project.movietime.connection;

import ispw.project.movietime.exception.DatabaseException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SingletonDatabase {

    private Connection connection;

    private SingletonDatabase() {
        initializeConnection();
    }

    private void initializeConnection() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new DatabaseException("Unable to find database.properties file on the classpath.");
            }
            properties.load(input);

            String url = properties.getProperty("CONNECTION_URL");
            String user = properties.getProperty("USER");
            String password = properties.getProperty("PASSWORD");

            if (url == null || user == null || password == null) {
                throw new DatabaseException("One or more database properties (URL, USER, PASSWORD) are missing in database.properties.");
            }

            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(url, user, password);

        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL JDBC Driver not found! Please ensure it's in your classpath.", e);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to establish database connection. Check database server status, credentials, and URL.", e);
        } catch (Exception e) {
            throw new DatabaseException("An unexpected error occurred during database connection setup.", e);
        }
    }

    private static class SingletonHolder {
        private static final SingletonDatabase INSTANCE = new SingletonDatabase();
    }

    public static SingletonDatabase getInstance() throws DatabaseException {
        return SingletonHolder.INSTANCE;
    }

    public Connection getConnection() throws DatabaseException {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(5)) {
                closeConnection();
                initializeConnection();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking database connection validity or re-establishing it.", e);
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();

            } catch (SQLException e) {
                throw new DatabaseException("Failed to close database connection gracefully.", e);
            } finally {
                connection = null;
            }
        }
    }
}
