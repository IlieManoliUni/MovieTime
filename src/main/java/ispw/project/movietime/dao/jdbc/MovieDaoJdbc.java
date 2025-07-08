package ispw.project.movietime.dao.jdbc;

import ispw.project.movietime.connection.SingletonDatabase;
import ispw.project.movietime.dao.MovieDao;
import ispw.project.movietime.dao.queries.CrudMovie;
import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.exception.CsvException;
import ispw.project.movietime.model.MovieModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovieDaoJdbc implements MovieDao {

    private static final Logger LOGGER = Logger.getLogger(MovieDaoJdbc.class.getName());

    @Override
    public MovieModel retrieveById(int id) throws CsvException, CrudQueriesException {
        Connection conn = null;
        MovieModel movie = null;

        try {
            conn = SingletonDatabase.getInstance().getConnection();
            movie = CrudMovie.getMovieById(conn, id);
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after retrieveById for movie ID {0}: {1}", new Object[]{id, e.getMessage()});
                }
            }
        }
        return movie;
    }

    @Override
    public void saveMovie(MovieModel movie) throws CsvException, CrudQueriesException {
        Connection conn = null;

        try {
            conn = SingletonDatabase.getInstance().getConnection();
            CrudMovie.addMovie(conn, movie);
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after saveMovie for movie ''{0}'': {1}", new Object[]{movie.getTitle(), e.getMessage()});
                }
            }
        }
    }

    @Override
    public List<MovieModel> retrieveAllMovies() throws CsvException, CrudQueriesException {
        Connection conn = null;
        List<MovieModel> movies = null;

        try {
            conn = SingletonDatabase.getInstance().getConnection();
            movies = CrudMovie.getAllMovies(conn);

            if (movies == null || movies.isEmpty()) {
                return new ArrayList<>();
            }
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after retrieveAllMovies: {0}", e.getMessage());
                }
            }
        }
        return movies;
    }
}