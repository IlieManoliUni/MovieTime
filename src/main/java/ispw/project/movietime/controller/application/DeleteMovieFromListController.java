package ispw.project.movietime.controller.application;

import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.dao.MovieDao;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.exception.DaoException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteMovieFromListController {

    private static final Logger LOGGER = Logger.getLogger(DeleteMovieFromListController.class.getName());

    private final ListDao listDao;
    private final MovieDao movieDao;
    private final ListMovie listMovieDao;

    private final ApplicationControllerProvider applicationControllerProvider= ApplicationControllerProvider.getInstance();
    public DeleteMovieFromListController() {
        this.listDao = applicationControllerProvider.getListDao() ;
        this.movieDao = applicationControllerProvider.getMovieDao();
        this.listMovieDao = applicationControllerProvider.getListMovieDao();
    }

    private UserModel convertUserBeanToModel(UserBean userBean) {
        if (userBean == null) {
            return null;
        }
        return new UserModel(userBean.getUsername(), userBean.getPassword());
    }

    public boolean deleteMovieFromList(int listId, int movieId, UserBean currentUserBean) throws DaoException {
        UserModel currentUserModel = convertUserBeanToModel(currentUserBean);

        if (currentUserModel == null || currentUserModel.getUsername() == null || currentUserModel.getUsername().trim().isEmpty()) {
            throw new DaoException("User not authenticated or invalid user data. Cannot remove movie from list.");
        }
        if (listId <= 0) {
            throw new DaoException("Invalid list ID provided.");
        }
        if (movieId <= 0) {
            throw new DaoException("Invalid movie ID provided.");
        }

        try {
            ListModel targetList = listDao.retrieveById(listId);
            MovieModel movieToRemove = movieDao.retrieveById(movieId);

            if (targetList == null) {
                LOGGER.log(Level.WARNING, "Target list with ID {0} not found.", listId);
                throw new DaoException("The specified list does not exist.");
            }
            if (movieToRemove == null) {
                LOGGER.log(Level.WARNING, "Movie with TMDB ID {0} not found in local database. Cannot remove from list.", movieId);
                throw new DaoException("The specified movie (TMDB ID: " + movieId + ") does not exist in our system.");
            }

            if (!targetList.getUsername().equals(currentUserModel.getUsername())) {
                LOGGER.log(Level.WARNING, "Unauthorized attempt. User ''{0}'' tried to remove movie from list ''{1}'' owned by ''{2}''.",
                        new Object[]{currentUserModel.getUsername(), listId, targetList.getUsername()});
                throw new DaoException("You are not authorized to modify this list.");
            }

            if (!targetList.containsMovie(movieToRemove)) {
                LOGGER.log(Level.INFO, "Movie ''{0}'' (TMDB ID: {1}) is not in list ''{2}'' (ID: {3}). No action needed.",
                        new Object[]{movieToRemove.getTitle(), movieId, targetList.getName(), listId});
                return false;
            }

            listMovieDao.removeMovieFromList(targetList, movieToRemove);

            LOGGER.log(Level.INFO, "Successfully removed Movie ''{0}'' (TMDB ID: {1}) from list ''{2}'' (ID: {3}).",
                    new Object[]{movieToRemove.getTitle(), movieId, targetList.getName(), listId});
            return true;

        } catch (DaoException e) {
            LOGGER.log(Level.SEVERE, "DAO error removing movie from list.");
            throw new DaoException("Failed to remove movie from list due to a system error.", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred in DeleteMovieFromListController while removing a movie from a list.");
            throw new DaoException("An unexpected system error occurred.", e);
        }
    }
}