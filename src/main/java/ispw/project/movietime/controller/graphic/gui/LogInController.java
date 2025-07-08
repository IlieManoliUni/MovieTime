package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.controller.application.LoginController;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager; // Your SessionManager

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogInController implements NavigableController, GraphicControllerGui.NeedsSessionUser {

    private static final Logger LOGGER = Logger.getLogger(LogInController.class.getName());
    private static final String SYSTEM_ERROR_TITLE = "System Error";
    private static final String SCREEN_HOME = "home";

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signInButton;

    @FXML
    private Label errorMessageLabel; // For general login errors (from app layer exceptions)

    @FXML
    private Label usernameErrorLabel; // For username specific errors (from UserBean validation)
    @FXML
    private Label passwordErrorLabel; // For password specific errors (from UserBean validation)

    @FXML
    private HBox headerBar; // Assuming FXML includes a header bar

    @FXML
    private DefaultController headerBarController; // Controller for the header bar

    private GraphicControllerGui graphicControllerGui;
    private final LoginController loginControllerIstance;
    private final SessionManager sessionManager; // Your SessionManager instance

    private final UserBean loginUserBean; // Local UserBean for UI input and validation

    // Listener for session changes, to trigger navigation or UI updates
    private final ChangeListener<UserBean> sessionChangeListener;

    /**
     * Constructs a LogInController, injecting its required application layer controller and
     * the SessionManager for observing global session state.
     */
    public LogInController() {
        this.loginControllerIstance = new LoginController();
        this.sessionManager = ApplicationControllerProvider.getInstance().getSessionManager();
        this.loginUserBean = new UserBean(); // Initialize the UserBean for local UI use

        // Define the ChangeListener for the session property
        this.sessionChangeListener = (observable, oldUserBean, newUserBean) -> {
            if (newUserBean != null) { // A user has logged in
                LOGGER.log(Level.INFO, "SessionManager detected user {0} logged in. Navigating to home.", newUserBean.getUsername());
                if (graphicControllerGui != null) {
                    graphicControllerGui.setScreen(SCREEN_HOME);
                } else {
                    LOGGER.log(Level.SEVERE, "GraphicControllerGui is null during session change, cannot navigate.");
                }
            } else { // No user logged in (e.g., logout or initial state)
                LOGGER.log(Level.INFO, "SessionManager detected no user logged in. Clearing login fields.");
                clearFields();
            }
        };
    }

    @Override
    public void setGraphicController(GraphicControllerGui graphicController) {
        this.graphicControllerGui = graphicController;

        if (headerBarController != null) {
            headerBarController.setGraphicController(this.graphicControllerGui);
        } else {
            LOGGER.log(Level.WARNING, "Header bar controller is null in LogInController. The header might not function correctly.");
        }
    }

    /**
     * This method is part of the `NeedsSessionUser` interface. It provides the `ObjectProperty`
     * of the current session user (`UserBean`) which can be observed by this controller.
     *
     * @param userBeanProperty The observable property holding the current session `UserBean`.
     */
    @Override
    public void setSessionUserProperty(ObjectProperty<UserBean> userBeanProperty) {
        // Add the listener to the session property so this controller reacts to login/logout
        userBeanProperty.addListener(sessionChangeListener);

        // Also check initial state in case the user is already logged in when this screen loads
        if (userBeanProperty.get() != null) {
            LOGGER.log(Level.INFO, "LogInController initialized with an existing logged-in user: {0}. Navigating to home.", userBeanProperty.get().getUsername());
            if (graphicControllerGui != null) {
                graphicControllerGui.setScreen(SCREEN_HOME);
            }
        }
    }

    @FXML
    private void initialize() {
        // Bind UI input fields to the local UserBean's properties for client-side validation
        if (usernameField != null) {
            usernameField.textProperty().bindBidirectional(loginUserBean.usernameProperty());
        }
        if (passwordField != null) {
            passwordField.textProperty().bindBidirectional(loginUserBean.passwordProperty());
        }

        // Bind error labels to the local UserBean's error properties
        if (usernameErrorLabel != null) {
            usernameErrorLabel.textProperty().bind(loginUserBean.usernameErrorProperty());
        }
        if (passwordErrorLabel != null) {
            passwordErrorLabel.textProperty().bind(loginUserBean.passwordErrorProperty());
        }

        // Initialize and bind the header bar controller with the SessionManager
        if (headerBarController != null && headerBarController instanceof GraphicControllerGui.NeedsSessionUser headerNeedsSession) {
            headerNeedsSession.setSessionUserProperty(sessionManager.currentUserBeanProperty());
        }

        // Ensure fields are cleared initially if no one is logged in
        if (!sessionManager.isLoggedIn()) {
            clearFields();
        }

        // Note: The `errorMessageLabel` will be set directly via `setText` when an `ExceptionApplication` is caught.
        // It's not bound to an observable property on SessionManager in this revised setup because
        // `SessionManager` doesn't expose an error message property, and we're strictly
        // avoiding adding new attributes to existing models/beans.
    }

    @FXML
    private void handleLoginButtonAction() {
        // Clear previous error messages
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("");
        }

        // Trigger client-side validation using the local UserBean
        if (!loginUserBean.isValid()) {
            LOGGER.log(Level.INFO, "Client-side input validation failed for login. Errors displayed on UI.");
            // UserBean's error properties are already bound to usernameErrorLabel/passwordErrorLabel.
            return; // Stop if client-side validation fails
        }

        try {
            // Delegate the login request to the application layer, passing the local UserBean
            loginControllerIstance.authenticateUser(loginUserBean);

            // If authenticateUser completes without throwing an exception,
            // the SessionManager has been updated, and our listener will handle navigation.
            LOGGER.log(Level.INFO, "Login request sent for user {0}. UI will update based on SessionManager changes.", loginUserBean.getUsername());

        } catch (DaoException e) {
            // Catch business logic exceptions from the application layer
            showAlert(Alert.AlertType.ERROR, "Login Failed", e.getMessage());
            if (errorMessageLabel != null) {
                errorMessageLabel.setText(e.getMessage()); // Display the specific error message
            }
            LOGGER.log(Level.WARNING, "Login application exception for user {0}: {1}", new Object[]{loginUserBean.getUsername(), e.getMessage()});
        } catch (Exception e) {
            // Catch any other unexpected system-level errors
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred during login: " + e.getMessage());
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("An unexpected system error occurred.");
            }
            LOGGER.log(Level.SEVERE, "Unexpected error during login for user {0}: {1}", new Object[]{loginUserBean.getUsername(), e.getMessage()});
        }
    }

    @FXML
    private void handleSignInButtonAction() {
        if (graphicControllerGui != null) {
            graphicControllerGui.setScreen("signIn");
        } else {
            LOGGER.log(Level.SEVERE, "GraphicControllerGui is null when attempting to navigate to sign in.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void clearFields() {
        // Clear the JavaFX input fields
        if (usernameField != null) usernameField.setText("");
        if (passwordField != null) passwordField.setText("");

        // Clear the local UserBean's properties, which also clears its validation error messages
        loginUserBean.setUsername("");
        loginUserBean.setPassword("");

        // Clear the general error message label
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("");
        }
    }
}