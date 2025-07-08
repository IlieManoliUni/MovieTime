package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.controller.application.LoginController;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

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
    private Label errorMessageLabel;

    @FXML
    private Label usernameErrorLabel;
    @FXML
    private Label passwordErrorLabel;

    @FXML
    private HBox headerBar;

    @FXML
    private DefaultController headerBarController;

    private GraphicControllerGui graphicControllerGui;
    private final LoginController loginControllerIstance;
    private final SessionManager sessionManager;

    private final UserBean loginUserBean;

    private final ChangeListener<UserBean> sessionChangeListener;

    public LogInController() {
        this.loginControllerIstance = new LoginController();
        this.sessionManager = ApplicationControllerProvider.getInstance().getSessionManager();
        this.loginUserBean = new UserBean();

        this.sessionChangeListener = (observable, oldUserBean, newUserBean) -> {
            if (newUserBean != null) {
                LOGGER.log(Level.INFO, "SessionManager detected user {0} logged in. Navigating to home.", newUserBean.getUsername());
                if (graphicControllerGui != null) {
                    graphicControllerGui.setScreen(SCREEN_HOME);
                } else {
                    LOGGER.log(Level.SEVERE, "GraphicControllerGui is null during session change, cannot navigate.");
                }
            } else {
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

    @Override
    public void setSessionUserProperty(ObjectProperty<UserBean> userBeanProperty) {
        userBeanProperty.addListener(sessionChangeListener);

        if (userBeanProperty.get() != null) {
            LOGGER.log(Level.INFO, "LogInController initialized with an existing logged-in user: {0}. Navigating to home.", userBeanProperty.get().getUsername());
            if (graphicControllerGui != null) {
                graphicControllerGui.setScreen(SCREEN_HOME);
            }
        }
    }

    @FXML
    private void initialize() {
        if (usernameField != null) {
            usernameField.textProperty().bindBidirectional(loginUserBean.usernameProperty());
        }
        if (passwordField != null) {
            passwordField.textProperty().bindBidirectional(loginUserBean.passwordProperty());
        }

        if (usernameErrorLabel != null) {
            usernameErrorLabel.textProperty().bind(loginUserBean.usernameErrorProperty());
        }
        if (passwordErrorLabel != null) {
            passwordErrorLabel.textProperty().bind(loginUserBean.passwordErrorProperty());
        }

        if (headerBarController != null && headerBarController instanceof GraphicControllerGui.NeedsSessionUser headerNeedsSession) {
            headerNeedsSession.setSessionUserProperty(sessionManager.currentUserBeanProperty());
        }

        if (!sessionManager.isLoggedIn()) {
            clearFields();
        }

    }

    @FXML
    private void handleLoginButtonAction() {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("");
        }

        if (!loginUserBean.isValid()) {
            LOGGER.log(Level.INFO, "Client-side input validation failed for login. Errors displayed on UI.");
            return;
        }

        try {
            loginControllerIstance.authenticateUser(loginUserBean);

            LOGGER.log(Level.INFO, "Login request sent for user {0}. UI will update based on SessionManager changes.", loginUserBean.getUsername());

        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", e.getMessage());
            if (errorMessageLabel != null) {
                errorMessageLabel.setText(e.getMessage());
            }
            LOGGER.log(Level.WARNING, "Login application exception for user {0}: {1}", new Object[]{loginUserBean.getUsername(), e.getMessage()});
        } catch (Exception e) {
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
        if (usernameField != null) usernameField.setText("");
        if (passwordField != null) passwordField.setText("");

        loginUserBean.setUsername("");
        loginUserBean.setPassword("");

        if (errorMessageLabel != null) {
            errorMessageLabel.setText("");
        }
    }
}