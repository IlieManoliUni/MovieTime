package ispw.project.movietime.dao.jdbc;

import ispw.project.movietime.connection.SingletonDatabase;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.dao.queries.CrudListMovie;
import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.exception.CsvException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListMovieDaoJdbc implements ListMovie {

    private static final Logger LOGGER = Logger.getLogger(ListMovieDaoJdbc.class.getName());

    @Override
    public void addMovieToList(ListModel list, MovieModel movie) throws CsvException, CrudQueriesException {
        Connection conn = null;
        try {
            conn = SingletonDatabase.getInstance().getConnection();
            CrudListMovie.addMovieToList(conn, list, movie);
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after addMovieToList: {0}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void removeMovieFromList(ListModel list, MovieModel movie) throws CsvException, CrudQueriesException {
        Connection conn = null;
        try {
            conn = SingletonDatabase.getInstance().getConnection();
            CrudListMovie.removeMovieFromList(conn, list, movie);
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after removeMovieFromList: {0}", e.getMessage());
                }
            }
        }
    }

    @Override
    public List<MovieModel> getAllMoviesInList(ListModel list) throws CsvException, CrudQueriesException {
        Connection conn = null;
        List<MovieModel> movies;
        try {
            conn = SingletonDatabase.getInstance().getConnection();
            movies = CrudListMovie.getMoviesFullDetailsByList(conn, list);

            if (movies == null) {
                return new ArrayList<>();
            }
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after getAllMoviesInList: {0}", e.getMessage());
                }
            }
        }
        return movies;
    }

    @Override
    public void removeAllMoviesFromList(ListModel list) throws CsvException, CrudQueriesException {
        Connection conn = null;
        try {
            conn = SingletonDatabase.getInstance().getConnection();
            CrudListMovie.removeAllMoviesFromList(conn, list);
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after removeAllMoviesFromList: {0}", e.getMessage());
                }
            }
        }
    }
}