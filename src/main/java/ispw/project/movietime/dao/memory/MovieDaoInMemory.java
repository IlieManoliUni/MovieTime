package ispw.project.movietime.dao.memory;

import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.dao.MovieDao;
import ispw.project.movietime.exception.DaoException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovieDaoInMemory implements MovieDao {

    private static final Logger LOGGER = Logger.getLogger(MovieDaoInMemory.class.getName());

    private final Map<Integer, MovieModel> movieMap = new HashMap<>();



    @Override
    public MovieModel retrieveById(int id) throws DaoException {
        LOGGER.log(Level.INFO, "Attempting to retrieve movie by ID: {0}", id);
        return movieMap.get(id);
    }

    @Override
    public void saveMovie(MovieModel movie) throws DaoException {
        if (movie == null) {
            throw new IllegalArgumentException("Movie model cannot be null.");
        }
        int id = movie.getId();

        if (movieMap.containsKey(id)) {
            LOGGER.log(Level.WARNING, "Attempted to save movie with ID {0}, but it already exists.", id);
            throw new DaoException("Movie with ID " + id + " already exists. Use update if you intend to modify.");
        }

        movieMap.put(id, movie);
        LOGGER.log(Level.INFO, "Movie saved successfully to in-memory storage.");
    }

    @Override
    public List<MovieModel> retrieveAllMovies() throws DaoException {
        if (movieMap.isEmpty()) {
            LOGGER.log(Level.INFO, "No movies found in in-memory storage.");
            return new ArrayList<>();
        }
        LOGGER.log(Level.INFO, "Retrieving all {0} movies from in-memory storage.", movieMap.size());
        return Collections.unmodifiableList(new ArrayList<>(movieMap.values()));
    }
}