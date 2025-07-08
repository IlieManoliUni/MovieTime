package ispw.project.movietime.dao;

import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.UserModel;

public interface UserDao {

    UserModel retrieveByUsername(String username) throws DaoException, CrudQueriesException;

    void saveUser(UserModel user) throws DaoException, CrudQueriesException;
}
