package ispw.project.movietime.controller.application;

import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.bean.ListBean;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeeAllElementsListController {

    private static final Logger LOGGER = Logger.getLogger(SeeAllElementsListController.class.getName());

    private final ListDao listDao;
    private final ListMovie listMovieDao; // Add ListMovie DAO

    private final ApplicationControllerProvider applicationControllerProvider= ApplicationControllerProvider.getInstance();

    public SeeAllElementsListController() {
        this.listDao = applicationControllerProvider.getListDao();
        this.listMovieDao = applicationControllerProvider.getListMovieDao();
    }

    private UserModel convertUserBeanToModel(UserBean userBean) {
        if (userBean == null) {
            return null;
        }
        return new UserModel(userBean.getUsername(), userBean.getPassword());
    }

    private ListModel validateAndRetrieveListModel(int listId, UserModel currentUserModel) throws DaoException, CrudQueriesException {
        if (currentUserModel == null || currentUserModel.getUsername() == null || currentUserModel.getUsername().trim().isEmpty()) {
            throw new DaoException("User not authenticated or invalid user data. Cannot retrieve list elements.");
        }
        if (listId <= 0) {
            throw new DaoException("Invalid list ID provided.");
        }

        ListModel list = listDao.retrieveById(listId);

        if (list == null) {
            LOGGER.log(Level.WARNING, "List with ID {0} not found for user {1}", new Object[]{listId, currentUserModel.getUsername()});
            throw new DaoException("List not found.");
        }

        if (!list.getUsername().equals(currentUserModel.getUsername())) {
            LOGGER.log(Level.WARNING, "Unauthorized access attempt. User ''{0}'' tried to access list ''{1}'' owned by ''{2}''",
                    new Object[]{currentUserModel.getUsername(), listId, list.getUsername()});
            throw new DaoException("You are not authorized to view this list.");
        }
        return list;
    }

    public ListBean getListDetailsForUser(int listId, UserBean currentUserBean) throws DaoException {
        UserModel currentUserModel = convertUserBeanToModel(currentUserBean);
        try {
            ListModel listModel = validateAndRetrieveListModel(listId, currentUserModel);
            return new ListBean(listModel);
        } catch (DaoException e) {
            throw e;
        } catch (Exception e) {
            throw new DaoException("An unexpected system error occurred while retrieving list details.", e);
        }
    }

    public List<MovieBean> seeAllMoviesInList(int listId, UserBean currentUserBean) throws DaoException {
        UserModel currentUserModel = convertUserBeanToModel(currentUserBean);

        try {
            ListModel list = validateAndRetrieveListModel(listId, currentUserModel);

            LOGGER.log(Level.INFO, "SeeAllElementsListController: Getting movies for list ID {0} ({1}) via ListMovieDao",
                    new Object[]{list.getId(), list.getName()});
            List<MovieModel> movieModels = listMovieDao.getAllMoviesInList(list);

            if (movieModels != null) {
                LOGGER.log(Level.INFO, "SeeAllElementsListController: Found {0} movies for list ID {1}",
                        new Object[]{movieModels.size(), list.getId()});
                return movieModels.stream()
                        .map(MovieBean::new)
                        .toList();
            } else {
                LOGGER.log(Level.WARNING, "SeeAllElementsListController: getAllMoviesInList returned null for list ID {0}", list.getId());
                return Collections.emptyList();
            }
        } catch (DaoException e) {
            throw e;
        } catch (Exception e) {
            throw new DaoException("An unexpected system error occurred.", e);
        }
    }
}