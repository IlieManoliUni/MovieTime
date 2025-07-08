package ispw.project.movietime.controller.application;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.exception.DaoException;

import java.util.List;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetAllListsController {

    private static final Logger LOGGER = Logger.getLogger(GetAllListsController.class.getName());

    private ListDao listDao;
    private final ApplicationControllerProvider applicationControllerProvider= ApplicationControllerProvider.getInstance();
    public GetAllListsController() {
        this.listDao = applicationControllerProvider.getListDao();
    }

    public List<ListBean> getAllListsForUser(UserBean currentUserBean) throws DaoException {
        if (currentUserBean == null || currentUserBean.getUsername() == null || currentUserBean.getUsername().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "GetAllListsController: Invalid user data provided. UserBean: {0}", currentUserBean);
            throw new DaoException("User not authenticated or invalid user data. Cannot retrieve lists.");
        }

        try {
            UserModel currentUserModel = new UserModel(currentUserBean.getUsername(), null);

            List<ListModel> userListsModels = listDao.retrieveAllListsOfUsername(currentUserModel.getUsername());

            if (userListsModels == null || userListsModels.isEmpty()) {
                return Collections.emptyList();
            }

            return userListsModels.stream()
                    .map(ListBean::new)
                    .toList();

        } catch (DaoException e) {
            throw new DaoException("Failed to retrieve user's lists due to a system error.", e);
        } catch (Exception e) {
            throw new DaoException("An unexpected system error occurred while retrieving lists.", e);
        }
    }
}