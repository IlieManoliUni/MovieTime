package ispw.project.movietime.dao.queries;

import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.UserModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrudList {

    private static final Logger LOGGER = Logger.getLogger(CrudList.class.getName());

    private CrudList(){
        //Empty Constructor
    }

    private static final String INSERT_LIST_SQL = "INSERT INTO list (name, username) VALUES (?, ?)";
    private static final String UPDATE_LIST_SQL = "UPDATE list SET name=?, username=? WHERE idList = ?";
    private static final String DELETE_LIST_SQL = "DELETE FROM list WHERE idList = ?";
    private static final String SELECT_ALL_LISTS_SQL = "SELECT idList, name, username FROM list";
    private static final String SELECT_LIST_BY_ID_SQL = "SELECT idList, name, username FROM list WHERE idList = ?";
    private static final String SELECT_LISTS_BY_USERNAME_SQL = "SELECT idList, name, username FROM list WHERE username = ?";

    public static void addList(Connection conn, ListModel list, UserModel user) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_LIST_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, list.getName());
            ps.setString(2, user.getUsername());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new CrudQueriesException("Creating list failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    list.setId(generatedId);
                    LOGGER.log(Level.INFO, "List ''{0}'' saved with generated ID: {1}", new Object[]{list.getName(), generatedId});
                } else {
                    throw new CrudQueriesException("Creating list failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to add list: " + e.getMessage(), e);
        }
    }

    public static int updateList(Connection conn, ListModel list, UserModel user) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_LIST_SQL)) {
            ps.setString(1, list.getName());
            ps.setString(2, user.getUsername());
            ps.setInt(3, list.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to update list with ID " + list.getId() + ": " + e.getMessage(), e);
        }
    }

    public static int deleteList(Connection conn, int listId) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_LIST_SQL)) {
            ps.setInt(1, listId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to delete list with ID " + listId + ": " + e.getMessage(), e);
        }
    }

    public static void printAllLists(Connection conn) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_LISTS_SQL)){
            ps.executeQuery();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to print all lists: " + e.getMessage(), e);
        }
    }

    public static List<ListModel> getAllLists(Connection conn) throws CrudQueriesException {
        List<ListModel> lists = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_LISTS_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lists.add(mapResultSetToListModel(rs));
            }
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to retrieve all lists: " + e.getMessage(), e);
        }
        return lists;
    }

    public static ListModel getListById(Connection conn, int id) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_LIST_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToListModel(rs);
                }
            }
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to retrieve list by ID " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public static List<ListModel> getListsByUsername(Connection conn, String username) throws CrudQueriesException {
        List<ListModel> userLists = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_LISTS_BY_USERNAME_SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userLists.add(mapResultSetToListModel(rs));
                }
            }
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to retrieve lists for username " + username + ": " + e.getMessage(), e);
        }
        return userLists;
    }

    private static ListModel mapResultSetToListModel(ResultSet rs) throws SQLException {
        int idList = rs.getInt("idList");
        String name = rs.getString("name");
        String username = rs.getString("username");

        return new ListModel(idList, name, username);
    }
}