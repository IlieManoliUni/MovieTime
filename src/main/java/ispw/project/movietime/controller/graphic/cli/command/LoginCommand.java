package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.LoginController;
import ispw.project.movietime.exception.UserException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

public class LoginCommand implements CliCommand {


    private final LoginController loginController;

    public LoginCommand( ) {
        this.loginController = new LoginController();
    }

    @Override
    public String execute(String args) throws UserException {

        String[] loginArgs = args.trim().split(" ", 2);
        if (loginArgs.length < 2) {
            throw new UserException("Usage: login <username> <password>");
        }

        String username = loginArgs[0];
        String password = loginArgs[1];

        UserBean userBean = new UserBean(username, password);

        try {
            loginController.authenticateUser(userBean);

            SessionManager sessionManager = SessionManager.getInstance();
            if (sessionManager.isLoggedIn()) {
                UserBean loggedInUser = sessionManager.getCurrentUserBean();
                return "User '" + loggedInUser.getUsername() + "' logged in successfully.";
            } else {
                throw new UserException("Login failed due to an unexpected session error. Please try again.");
            }

        } catch (DaoException e) {
            throw new UserException("Login failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new UserException("An unexpected system error occurred during login. Please try again.", e);
        }
    }
}