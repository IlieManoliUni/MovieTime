package ispw.project.movietime.dao;

import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.UserModel;

import java.util.List;

public interface ListDao {

    ListModel retrieveById(int id) throws DaoException, CrudQueriesException;

    void saveList(ListModel list, UserModel user) throws DaoException;

    void deleteList(ListModel list) throws DaoException, CrudQueriesException;

    List<ListModel> retrieveAllListsOfUsername(String username) throws DaoException, CrudQueriesException;
}
