package ispw.project.movietime.controller.application;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.exception.DaoException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteListController {

    private static final Logger LOGGER = Logger.getLogger(DeleteListController.class.getName());

    private ListDao listDao;

    private final ApplicationControllerProvider applicationControllerProvider= ApplicationControllerProvider.getInstance();

    public DeleteListController() {
        this.listDao = applicationControllerProvider.getListDao();
    }

    public void deleteList(ListBean listToDeleteBean, UserBean currentUserBean) throws DaoException {
        if (currentUserBean == null || currentUserBean.getUsername() == null || currentUserBean.getUsername().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "DeleteListController: User not authenticated or invalid user data provided for deletion. UserBean: {0}", currentUserBean);
            throw new DaoException("You must be logged in to delete a list.");
        }
        if (listToDeleteBean == null || listToDeleteBean.getId() <= 0) {
            LOGGER.log(Level.WARNING, "DeleteListController: Invalid list data provided for deletion. ListBean: {0}", listToDeleteBean);
            throw new DaoException("Invalid list selected for deletion. Please try again.");
        }

        try {
            UserModel currentUserModel = new UserModel(currentUserBean.getUsername(), null);
            ListModel listModelFromDb = listDao.retrieveById(listToDeleteBean.getId());

            if (listModelFromDb == null) {
                LOGGER.log(Level.WARNING, "DeleteListController: List with ID {0} (Name: {1}) not found in DB for deletion.",
                        new Object[]{listToDeleteBean.getId(), listToDeleteBean.getListName()});
                throw new DaoException("The list you tried to delete was not found. It might have been deleted already.");
            }

            if (!listModelFromDb.getUsername().equals(currentUserModel.getUsername())) {
                throw new DaoException("You are not authorized to delete this list. You do not own it.");
            }

            listDao.deleteList(listModelFromDb);

            LOGGER.log(Level.INFO, "List successfully deleted.");


        } catch (DaoException e) {
            throw new DaoException("Failed to delete list due to a database error. Please try again later.", e);
        } catch (Exception e) {
            throw new DaoException("An unexpected system error occurred while deleting the list. Please contact support.", e);
        }
    }
}