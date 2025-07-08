package ispw.project.movietime.dao.jdbc;

import ispw.project.movietime.connection.SingletonDatabase;
import ispw.project.movietime.dao.UserDao;
import ispw.project.movietime.dao.queries.CrudUser;
import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.exception.CsvException;
import ispw.project.movietime.model.UserModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDaoJdbc implements UserDao {

    private static final Logger LOGGER = Logger.getLogger(UserDaoJdbc.class.getName());

    @Override
    public UserModel retrieveByUsername(String username) throws CsvException, CrudQueriesException {
        Connection conn = null;
        UserModel user = null;

        try {
            conn = SingletonDatabase.getInstance().getConnection();
            user = CrudUser.getUserByUsername(conn, username);
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after retrieveByUsername for user ''{0}'': {1}", new Object[]{username, e.getMessage()});
                }
            }
        }
        return user;
    }

    @Override
    public void saveUser(UserModel user) throws CsvException, CrudQueriesException {
        Connection conn = null;

        try {
            conn = SingletonDatabase.getInstance().getConnection();
            UserModel existingUser = CrudUser.getUserByUsername(conn, user.getUsername());

            if (existingUser != null) {
                throw new CsvException("User already exists with username: " + user.getUsername());
            }

            CrudUser.addUser(conn, user);
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after saveUser for user ''{0}'': {1}", new Object[]{user.getUsername(), e.getMessage()});
                }
            }
        }
    }
}