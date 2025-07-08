package ispw.project.movietime.controller.application;

import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.dao.UserDao;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.session.SessionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    private final UserDao userDao;
    private final SessionManager sessionManager;

    private final ApplicationControllerProvider applicationControllerProvider= ApplicationControllerProvider.getInstance();

    public LoginController() {
        this.userDao = applicationControllerProvider.getUserDao();
        this.sessionManager = applicationControllerProvider.getSessionManager();
    }

    public void authenticateUser(UserBean userBean) throws DaoException {
        sessionManager.logout();

        String username = userBean.getUsername();
        String password = userBean.getPassword();
        userBean.setPassword("");

        try {
            UserModel authenticatedUser = userDao.retrieveByUsername(username);
            if (authenticatedUser == null) {

                String msg = "Invalid username or password.";
                LOGGER.log(Level.WARNING, "Authentication failed: User ''{0}'' not found.", username);
                throw new DaoException(msg);
            }

            if (!authenticatedUser.verifyPassword(password)) {
                String msg = "Invalid username or password.";
                LOGGER.log(Level.WARNING, "Authentication failed: Incorrect password for user ''{0}''.", username);
                throw new DaoException(msg);
            }

            sessionManager.login(authenticatedUser);

            LOGGER.log(Level.INFO, "User {0} logged in successfully.", authenticatedUser.getUsername());

        } catch (DaoException e) {
            String msg = "A system error occurred during login. Please try again later.";
            LOGGER.log(Level.SEVERE, "DAO error during user login.");
            throw new DaoException(msg, e);
        } catch (Exception e) {
            String msg = "An unexpected system error occurred during login.";
            LOGGER.log(Level.SEVERE, "An unexpected error occurred in LoginController.");
            throw new DaoException(msg, e);
        }
    }

    public void logout() {
        sessionManager.logout();
    }
}