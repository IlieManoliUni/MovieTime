package ispw.project.movietime.dao.jdbc;

import ispw.project.movietime.connection.SingletonDatabase;
import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.dao.queries.CrudList;
import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.model.UserModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListDaoJdbc implements ListDao {

    private static final Logger LOGGER = Logger.getLogger(ListDaoJdbc.class.getName());

    private final ListMovie listMovieDao;

    public ListDaoJdbc() {
        this.listMovieDao = new ListMovieDaoJdbc();
    }

    @Override
    public ListModel retrieveById(int id) throws DaoException {
        Connection conn = null;
        ListModel listModel = null;

        try {
            conn = SingletonDatabase.getInstance().getConnection();
            listModel = CrudList.getListById(conn, id);

            if (listModel != null) {
                List<MovieModel> movies = listMovieDao.getAllMoviesInList(listModel);
                listModel.setMovies(movies);
            }
        } catch (CrudQueriesException e) {
            throw new DaoException("Error from database query: " + e.getMessage(), e);
        } catch (RuntimeException | DaoException e) {
            throw new DaoException("An unexpected system error occurred during DAO operation.", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after retrieveById for list ID {0}: {1}", new Object[]{id, e.getMessage()});
                }
            }
        }
        return listModel;
    }

    @Override
    public void saveList(ListModel list, UserModel user) throws DaoException {
        Connection conn = null;
        try {
            conn = SingletonDatabase.getInstance().getConnection();
            CrudList.addList(conn, list, user);
        } catch (CrudQueriesException e) {
            throw new DaoException("Error from database query: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after saveList for list ''{0}'': {1}", new Object[]{list.getName(), e.getMessage()});
                }
            }
        }
    }

    @Override
    public void deleteList(ListModel list) throws DaoException {
        Connection conn = null;
        try {
            conn = SingletonDatabase.getInstance().getConnection();
            CrudList.deleteList(conn, list.getId());
        } catch (CrudQueriesException e) {
            throw new DaoException("Error from database query: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after deleteList for list ID {0}: {1}", new Object[]{list.getId(), e.getMessage()});
                }
            }
        }
    }

    @Override
    public List<ListModel> retrieveAllListsOfUsername(String username) throws DaoException {
        Connection conn = null;
        List<ListModel> lists = new ArrayList<>();

        try {
            conn = SingletonDatabase.getInstance().getConnection();
            List<ListModel> fetchedLists = CrudList.getListsByUsername(conn, username);

            if (fetchedLists != null) {
                for (ListModel listModel : fetchedLists) {
                    List<MovieModel> movies = listMovieDao.getAllMoviesInList(listModel);
                    listModel.setMovies(movies);
                    lists.add(listModel);
                }
            }
        } catch (CrudQueriesException e) {
            throw new DaoException("Error from database query: " + e.getMessage(), e);
        } catch (RuntimeException | DaoException e) {
            throw new DaoException("An unexpected system error occurred during DAO operation.", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection after retrieveAllListsOfUsername for user ''{0}'': {1}", new Object[]{username, e.getMessage()});
                }
            }
        }
        return lists;
    }
}