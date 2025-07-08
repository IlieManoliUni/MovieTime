package ispw.project.movietime.controller.application;

import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.session.SessionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogoutController {

    private static final Logger LOGGER = Logger.getLogger(LogoutController.class.getName());

    private final SessionManager sessionManager;

    private final ApplicationControllerProvider applicationControllerProvider= ApplicationControllerProvider.getInstance();

    public LogoutController() {
        this.sessionManager = applicationControllerProvider.getSessionManager();
        LOGGER.log(Level.INFO, "LogoutController initialized.");
    }

    public void logoutUser() {
        sessionManager.logout();
        LOGGER.log(Level.INFO, "User logout request processed by LogoutController.");
    }
}