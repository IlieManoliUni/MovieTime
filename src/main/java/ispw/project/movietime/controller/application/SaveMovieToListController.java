package ispw.project.movietime.controller.application;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.bean.UserBean;

import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.model.UserModel;

import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.dao.MovieDao;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.exception.DaoException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SaveMovieToListController {

    private static final Logger LOGGER = Logger.getLogger(SaveMovieToListController.class.getName());

    private ListDao listDao;
    private MovieDao movieDao;
    private ListMovie listMovieDao;

    private final ApplicationControllerProvider applicationControllerProvider = ApplicationControllerProvider.getInstance();

    public SaveMovieToListController() {
        this.listDao = applicationControllerProvider.getListDao();
        this.movieDao = applicationControllerProvider.getMovieDao();
        this.listMovieDao = applicationControllerProvider.getListMovieDao();
    }

    public ListBean findListForUserByName(UserBean userBean, String listName) throws DaoException {
        if (userBean == null || userBean.getUsername() == null || userBean.getUsername().trim().isEmpty()) {
            throw new DaoException("User not provided or invalid username. Cannot find list.");
        }
        if (listName == null || listName.trim().isEmpty()) {
            throw new DaoException("List name cannot be empty. Cannot find list.");
        }

        try {

            UserModel userModel = new UserModel(userBean.getUsername(), null);

            List<ListModel> userLists = listDao.retrieveAllListsOfUsername(userModel.getUsername());

            for (ListModel listModel : userLists) {
                if (listModel.getName() != null && listModel.getName().equals(listName)) {
                    return new ListBean(listModel);
                }
            }
            return null;

        } catch (DaoException e) {
            throw new DaoException("Failed to retrieve list due to a system error.", e);
        } catch (Exception e) {
            throw new DaoException("An unexpected system error occurred while finding list.", e);
        }
    }


    public boolean saveMovieToList(ListBean targetListBean, MovieBean movieBeanToAdd, UserBean currentUserBean) throws DaoException {
        validateSaveMovieToListInputs(targetListBean, movieBeanToAdd, currentUserBean);

        try {
            UserModel currentUserModel = new UserModel(currentUserBean.getUsername(), null);
            ListModel targetListModel = retrieveAndValidateList(targetListBean, currentUserModel);

            MovieModel movieToAddModel = ensureMovieExistsInPersistence(movieBeanToAdd);


            if (targetListModel.containsMovie(movieToAddModel)) {
               return false;
            }

            listMovieDao.addMovieToList(targetListModel, movieToAddModel);

            return true;

        } catch (DaoException e) {
            throw new DaoException("Failed to add movie to list due to a system error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DaoException("An unexpected system error occurred.", e);
        }
    }


    private void validateSaveMovieToListInputs(ListBean targetListBean, MovieBean movieBeanToAdd, UserBean currentUserBean) throws DaoException {
        if (currentUserBean == null || currentUserBean.getUsername() == null || currentUserBean.getUsername().trim().isEmpty()) {
            throw new DaoException("User not authenticated or invalid user data. Cannot save movie to list.");
        }
        if (targetListBean == null || targetListBean.getId() <= 0) {
            throw new DaoException("Invalid list data provided.");
        }
        if (movieBeanToAdd == null || movieBeanToAdd.getId() <= 0) {
            throw new DaoException("Invalid movie data provided.");
        }
        if (movieBeanToAdd.getTitle() == null || movieBeanToAdd.getTitle().trim().isEmpty() || movieBeanToAdd.getRuntimeDisplay() == null || movieBeanToAdd.getRuntimeDisplay().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "MovieBean (ID: {0}) is missing title or runtime display string. Cannot save movie to persistence.", movieBeanToAdd.getId());
            throw new DaoException("Movie details (title and runtime display string) are required to save the movie to the database.");
        }
    }

    private ListModel retrieveAndValidateList(ListBean targetListBean, UserModel currentUserModel) throws DaoException, CrudQueriesException {
        ListModel targetListModel = listDao.retrieveById(targetListBean.getId());

        if (targetListModel == null) {
            LOGGER.log(Level.WARNING, "SaveMovieToListController: Target list with ID {0} not found.", targetListBean.getId());
            throw new DaoException("The specified list does not exist.");
        }

        if (!targetListModel.getUsername().equals(currentUserModel.getUsername())) {
            throw new DaoException("You are not authorized to modify this list.");
        }
        return targetListModel;
    }

    private MovieModel ensureMovieExistsInPersistence(MovieBean movieBeanToAdd) throws DaoException, CrudQueriesException {
        MovieModel existingMovie = movieDao.retrieveById(movieBeanToAdd.getId());

        if (existingMovie == null) {
            LOGGER.log(Level.INFO, "Movie with ID {0} not found in local movie database. Saving it now.", movieBeanToAdd.getId());

            int runtime = parseRuntimeFromDisplay(movieBeanToAdd.getRuntimeDisplay());
            if (runtime <= 0) {
                throw new DaoException("Invalid movie runtime provided. Cannot save movie to database.");
            }

            MovieModel movieToAddModel = new MovieModel(movieBeanToAdd.getId(), runtime, movieBeanToAdd.getTitle());
            movieDao.saveMovie(movieToAddModel);

            return movieToAddModel;
        } else {
            return existingMovie;
        }
    }

    private int parseRuntimeFromDisplay(String runtimeDisplay) {
        if (runtimeDisplay == null || runtimeDisplay.trim().isEmpty() || "N/A".equalsIgnoreCase(runtimeDisplay.trim())) {
            return 0;
        }
        try {
            String numericPart = runtimeDisplay.trim().split(" ")[0];
            return Integer.parseInt(numericPart);
        } catch (NumberFormatException _) {
            return 0;
        }
    }
}