package ispw.project.movietime.dao.memory;

import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.exception.DaoException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListMovieDaoInMemory implements ListMovie {

    private static final Logger LOGGER = Logger.getLogger(ListMovieDaoInMemory.class.getName());

    private final Map<Integer, List<MovieModel>> movieByListId = new HashMap<>();

    @Override
    public void addMovieToList(ListModel list, MovieModel movie) throws DaoException {
        if (list == null || movie == null) {
            throw new IllegalArgumentException("ListModel and MovieModel cannot be null.");
        }

        int listId = list.getId();
        int movieId = movie.getId();
        List<MovieModel> movieList = movieByListId.computeIfAbsent(listId, k -> new ArrayList<>());

        if (movieList.contains(movie)) {
            LOGGER.log(Level.WARNING, "Movie with ID {0} already exists in list {1}.", new Object[]{movieId, listId});
            throw new DaoException("Movie with ID " + movieId + " already exists in list " + listId + ".");
        }

        movieList.add(movie);
        LOGGER.log(Level.INFO, "Movie added to list).");
    }

    @Override
    public void removeMovieFromList(ListModel list, MovieModel movie) throws DaoException {
        if (list == null || movie == null) {
            throw new IllegalArgumentException("ListModel and MovieModel cannot be null.");
        }

        int listId = list.getId();
        int movieId = movie.getId();

        List<MovieModel> movieList = movieByListId.get(listId);

        if (movieList == null || !movieList.remove(movie)) {
            LOGGER.log(Level.WARNING, "Movie with ID {0} not found in list {1} for removal.", new Object[]{movieId, listId});
            throw new DaoException("Movie with ID " + movieId + " not found in list " + listId + ".");
        }

        if (movieList.isEmpty()) {
            movieByListId.remove(listId);
            LOGGER.log(Level.INFO, "List is now empty, removed its entry from associations.");
        }
        LOGGER.log(Level.INFO, "Movie removed from list ");
    }

    @Override
    public List<MovieModel> getAllMoviesInList(ListModel list) throws DaoException {
        if (list == null) {
            throw new IllegalArgumentException("ListModel cannot be null.");
        }

        int listId = list.getId();

        List<MovieModel> movieList = movieByListId.getOrDefault(listId, Collections.emptyList());

        LOGGER.log(Level.INFO, "Retrieved movies for list");
        return Collections.unmodifiableList(movieList);
    }

    @Override
    public void removeAllMoviesFromList(ListModel list) throws DaoException {
        if (list == null) {
            throw new IllegalArgumentException("ListModel cannot be null.");
        }

        int listId = list.getId();

        if (movieByListId.remove(listId) != null) {
            LOGGER.log(Level.INFO, "All movies removed from list");
        } else {
            LOGGER.log(Level.INFO, "Attempted to remove all movies from list but list had no associated movies.");
        }
    }
}