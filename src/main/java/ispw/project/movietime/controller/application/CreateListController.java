package ispw.project.movietime.controller.application;

import ispw.project.movietime.bean.ListBean; // Import ListBean
import ispw.project.movietime.bean.UserBean; // Import UserBean
import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.exception.DaoException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateListController {

    private static final Logger LOGGER = Logger.getLogger(CreateListController.class.getName());
    private final ApplicationControllerProvider applicationControllerProvider= ApplicationControllerProvider.getInstance();
    private ListDao listDao;

    public CreateListController() {
        this.listDao = applicationControllerProvider.getListDao();
    }

    public ListBean createNewList(String listName, UserBean currentUserBean) throws DaoException {
        if (currentUserBean == null || currentUserBean.getUsername() == null || currentUserBean.getUsername().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "CreateListController: User not authenticated or invalid user data. UserBean: {0}", currentUserBean);
            throw new DaoException("You must be logged in to create a list.");
        }
        if (listName == null || listName.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "CreateListController: Invalid list name provided: {0}", listName);
            throw new DaoException("List name cannot be empty.");
        }

        try {
            UserModel currentUserModel = new UserModel(currentUserBean.getUsername(), null);

            ListModel newListModel = new ListModel(0, listName, currentUserModel.getUsername());

            listDao.saveList(newListModel, currentUserModel);

            if (newListModel.getId() == 0) {
                LOGGER.log(Level.SEVERE, "CreateListController: DAO failed to set ID for new list {0}.", listName);
                throw new DaoException("Failed to retrieve ID for the newly created list.");
            }
            LOGGER.log(Level.INFO, "List ''{0}'' created successfully for user ''{1}'' with ID {2}.",
                    new Object[]{listName, currentUserBean.getUsername(), newListModel.getId()});

            return new ListBean(newListModel);

        } catch (DaoException e) {
            throw new DaoException("Failed to create list for user " + currentUserBean.getUsername() + " due to a system error. Please try again later.", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred in CreateListController while creating a list.");
            throw new DaoException("An unexpected system error occurred while creating the list.", e);
        }
    }
}