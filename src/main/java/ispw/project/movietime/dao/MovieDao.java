package ispw.project.movietime.dao;

import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.MovieModel;

import java.util.List;

public interface MovieDao {

    MovieModel retrieveById(int id) throws DaoException, CrudQueriesException;

    void saveMovie(MovieModel movie) throws DaoException, CrudQueriesException;

    List<MovieModel> retrieveAllMovies() throws DaoException, CrudQueriesException;
}
