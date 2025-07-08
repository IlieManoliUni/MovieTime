package ispw.project.movietime.dao.queries;

import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.model.MovieModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CrudMovie {

    private CrudMovie(){
        //Empty Constructor
    }

    private static final String INSERT_MOVIE_SQL = "INSERT INTO movie (idMovieTmdb, runtime, name) VALUES (?, ?, ?)";
    private static final String UPDATE_MOVIE_SQL = "UPDATE movie SET name=?, runtime=? WHERE idMovieTmdb = ?";
    private static final String DELETE_MOVIE_SQL = "DELETE FROM movie WHERE idMovieTmdb = ?";
    private static final String SELECT_ALL_MOVIES_SQL = "SELECT idMovieTmdb, runtime, name FROM movie";
    private static final String SELECT_MOVIE_BY_ID_SQL = "SELECT idMovieTmdb, runtime, name FROM movie WHERE idMovieTmdb = ?";

    public static int addMovie(Connection conn, MovieModel movie) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_MOVIE_SQL)) {
            ps.setInt(1, movie.getId());
            ps.setInt(2, movie.getRuntime());
            ps.setString(3, movie.getTitle());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to add movie: " + e.getMessage(), e);
        }
    }

    public static int updateMovie(Connection conn, MovieModel movie) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_MOVIE_SQL)) {
            ps.setString(1, movie.getTitle());
            ps.setInt(2, movie.getRuntime());
            ps.setInt(3, movie.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to update movie with ID " + movie.getId() + ": " + e.getMessage(), e);
        }
    }

    public static int deleteMovie(Connection conn, int movieId) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_MOVIE_SQL)) {
            ps.setInt(1, movieId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to delete movie with ID " + movieId + ": " + e.getMessage(), e);
        }
    }

    public static void printAllMovies(Connection conn) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_MOVIES_SQL)){
             ps.executeQuery();
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to print all movies: " + e.getMessage(), e);
        }
    }

    public static List<MovieModel> getAllMovies(Connection conn) throws CrudQueriesException {
        List<MovieModel> movieList = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_MOVIES_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                movieList.add(mapResultSetToMovieBean(rs));
            }
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to retrieve all movies: " + e.getMessage(), e);
        }
        return movieList;
    }

    public static MovieModel getMovieById(Connection conn, int id) throws CrudQueriesException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_MOVIE_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMovieBean(rs);
                }
            }
        } catch (SQLException e) {
            throw new CrudQueriesException("Failed to retrieve movie by ID " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    private static MovieModel mapResultSetToMovieBean(ResultSet rs) throws SQLException {
        int idMovieTmdb = rs.getInt("idMovieTmdb");
        int runtime = rs.getInt("runtime");
        String name = rs.getString("name");

        return new MovieModel(idMovieTmdb, runtime, name);
    }
}