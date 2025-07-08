package ispw.project.movietime.dao.queries;

import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.model.UserModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CrudUser {

    private CrudUser(){
        //Empty Constructor
    }

    private static final String INSERT_USER_SQL = "INSERT INTO user (username, password) VALUES (?, ?)";
    private static final String UPDATE_USER_SQL = "UPDATE user SET password=? WHERE username = ?";
    private static final String DELETE_USER_SQL = "DELETE FROM user WHERE username = ?";
    private static final String SELECT_ALL_USERS_SQL = "SELECT username, password FROM user";
    private static final String SELECT_USER_BY_USERNAME_SQL = "SELECT username, password FROM user WHERE username = ?";

    public static int addUser(Connection conn, UserModel user) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_USER_SQL)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to add user '" + user.getUsername() + "': " + e.getMessage(), e);
        }
    }

    public static int updateUser(Connection conn, UserModel user) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_USER_SQL)) {
            ps.setString(1, user.getPassword());
            ps.setString(2, user.getUsername());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to update user '" + user.getUsername() + "': " + e.getMessage(), e);
        }
    }

    public static int deleteUser(Connection conn, String username) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_USER_SQL)) {
            ps.setString(1, username);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to delete user '" + username + "': " + e.getMessage(), e);
        }
    }

    public static void printAllUsers(Connection conn) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_USERS_SQL)){
             ps.executeQuery();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to print all users: " + e.getMessage(), e);
        }
    }

    public static List<UserModel> getAllUsers(Connection conn) throws CrudQueriesException {
        List<UserModel> userList = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_USERS_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                userList.add(mapResultSetToUserBean(rs));
            }
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to retrieve all users: " + e.getMessage(), e);
        }
        return userList;
    }

    public static UserModel getUserByUsername(Connection conn, String username) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_USER_BY_USERNAME_SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserBean(rs);
                }
            }
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to retrieve user by username '" + username + "': " + e.getMessage(), e);
        }
        return null;
    }

    private static UserModel mapResultSetToUserBean(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String password = rs.getString("password");

        return new UserModel(username, password);
    }
}