package ispw.project.movietime.dao.queries;

import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CrudListMovie {

    private CrudListMovie(){
        //Empty Constructor
    }
    private static final String IDMOVIETMDB = "idMovieTmdb";

    private static final String INSERT_LIST_MOVIE_SQL = "INSERT INTO list_movie (idList, idMovieTmdb) VALUES (?, ?)";
    private static final String DELETE_LIST_MOVIE_SQL = "DELETE FROM list_movie WHERE idList = ? AND idMovieTmdb = ?";
    private static final String SELECT_MOVIE_IDS_IN_LIST_SQL = "SELECT idMovieTmdb FROM list_movie WHERE idList = ?";
    private static final String SELECT_FULL_DETAILS_MOVIES_IN_LIST_SQL =
            "SELECT m.idMovieTmdb, m.runtime, m.name " +
                    "FROM list_movie lm " +
                    "JOIN movie m ON lm.idMovieTmdb = m.idMovieTmdb " +
                    "WHERE lm.idList = ?";
    private static final String DELETE_ALL_MOVIES_FROM_LIST_SQL = "DELETE FROM list_movie WHERE idList = ?";


    public static int addMovieToList(Connection conn, ListModel list, MovieModel movie) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_LIST_MOVIE_SQL)) {
            ps.setInt(1, list.getId());
            ps.setInt(2, movie.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to add movie ID " + movie.getId() + " to list ID " + list.getId() + ": " + e.getMessage(), e);
        }
    }

    public static int removeMovieFromList(Connection conn, ListModel list, MovieModel movie) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_LIST_MOVIE_SQL)) {
            ps.setInt(1, list.getId());
            ps.setInt(2, movie.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to remove movie ID " + movie.getId() + " from list ID " + list.getId() + ": " + e.getMessage(), e);
        }
    }

    public static void printAllMoviesInList(Connection conn, ListModel list) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_MOVIE_IDS_IN_LIST_SQL)) {
            ps.setInt(1, list.getId());
            ps.executeQuery();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to print all movies in list ID " + list.getId() + ": " + e.getMessage(), e);
        }
    }

    public static List<Integer> getMovieIdsByList(Connection conn, ListModel list) throws CrudQueriesException {
        List<Integer> movieIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_MOVIE_IDS_IN_LIST_SQL)) {
            ps.setInt(1, list.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movieIds.add(rs.getInt(IDMOVIETMDB));
                }
            }
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to retrieve movie IDs for list ID " + list.getId() + ": " + e.getMessage(), e);
        }
        return movieIds;
    }

    public static List<MovieModel> getMoviesFullDetailsByList(Connection conn, ListModel list) throws CrudQueriesException {
        List<MovieModel> movieDetails = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_FULL_DETAILS_MOVIES_IN_LIST_SQL)) {
            ps.setInt(1, list.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movieDetails.add(new MovieModel(
                            rs.getInt(IDMOVIETMDB),
                            rs.getInt("runtime"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to retrieve full movie details for list ID " + list.getId() + ": " + e.getMessage(), e);
        }
        return movieDetails;
    }

    public static int removeAllMoviesFromList(Connection conn, ListModel list) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_ALL_MOVIES_FROM_LIST_SQL)) {
            ps.setInt(1, list.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to remove all movies from list ID " + list.getId() + ": " + e.getMessage(), e);
        }
    }
}