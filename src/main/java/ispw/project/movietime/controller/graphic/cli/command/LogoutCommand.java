package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.LogoutController;
import ispw.project.movietime.exception.UserException;
import ispw.project.movietime.session.SessionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogoutCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(LogoutCommand.class.getName());

    private final LogoutController logoutController;

    public LogoutCommand( ) {
        this.logoutController = new LogoutController();
    }

    @Override
    public String execute(String args) throws UserException {
        if (!args.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "LogoutCommand received unexpected arguments. Ignoring.");
        }

        SessionManager sessionManager = SessionManager.getInstance();

        if (sessionManager.isLoggedIn()) {
            UserBean currentUser = sessionManager.getCurrentUserBean();
            String username = (currentUser != null) ? currentUser.getUsername() : "Unknown User";

            try {
                logoutController.logoutUser();

                return "User '" + username + "' logged out successfully.";

            } catch (Exception e) {
                throw new UserException("An unexpected error occurred during logout. Please try again.", e);
            }
        } else {
            LOGGER.log(Level.INFO, "LogoutCommand: Attempted to log out, but no user was logged in.");
            return "No user is currently logged in.";
        }
    }
}