package ispw.project.movietime.dao;


import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;

import java.util.List;

public interface ListMovie {
    void addMovieToList(ListModel list, MovieModel movie) throws DaoException, CrudQueriesException;

    void removeMovieFromList(ListModel list, MovieModel movie) throws DaoException, CrudQueriesException;

    List<MovieModel> getAllMoviesInList(ListModel list) throws DaoException, CrudQueriesException;

    void removeAllMoviesFromList(ListModel list) throws DaoException, CrudQueriesException;
}