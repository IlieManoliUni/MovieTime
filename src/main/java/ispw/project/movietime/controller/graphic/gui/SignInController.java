package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.SignupController;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.beans.binding.Bindings;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SignInController implements NavigableController {

    private static final Logger LOGGER = Logger.getLogger(SignInController.class.getName());
    private static final String SYSTEM_ERROR_TITLE = "System Error";
    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String SIGNUP_FORM_PREFIX = "signup_form_view:";

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button signupButton;
    @FXML private Label errorMessageLabel;

    private GraphicControllerGui graphicControllerGui;
    private UserBean userBean;

    @FXML private HBox headerBar;
    @FXML private DefaultController headerBarController;

    public SignInController() {
        // Default Constructor
    }

    @Override
    public void setGraphicController(GraphicControllerGui graphicController) {
        this.graphicControllerGui = graphicController;

        if (headerBarController != null) {
            headerBarController.setGraphicController(this.graphicControllerGui);
        } else {
            LOGGER.log(Level.WARNING, "Header bar controller is null. The header might not function correctly.");
        }
    }

    @FXML
    private void initialize() {
        userBean = new UserBean();

        usernameField.textProperty().bindBidirectional(userBean.usernameProperty());
        passwordField.textProperty().bindBidirectional(userBean.passwordProperty());

        errorMessageLabel.textProperty().bind(
                Bindings.when(userBean.usernameErrorProperty().isNotEmpty())
                        .then(userBean.usernameErrorProperty())
                        .otherwise(Bindings.when(userBean.passwordErrorProperty().isNotEmpty())
                                .then(userBean.passwordErrorProperty())
                                .otherwise(""))
        );
        errorMessageLabel.getStyleClass().add("error-label");

        clearFields();
    }

    @FXML
    private void handleSignInButtonAction() {
        clearErrorMessage();

        if (!userBean.isValid()) {
            showAlert(Alert.AlertType.WARNING, "Input Validation", errorMessageLabel.getText());
            return;
        }

        registerUserWithApplicationController();
    }

    private void registerUserWithApplicationController() {
        SignupController signupController = new SignupController();

        try {
            String username = userBean.getUsername();
            String password = userBean.getPassword();

            String outcome = signupController.signup(username, password);

            if (outcome.startsWith(REDIRECT_PREFIX)) {
                String screenToRedirect = outcome.substring(REDIRECT_PREFIX.length());
                graphicControllerGui.setScreen(screenToRedirect);
                clearFields();
            } else if (outcome.startsWith(SIGNUP_FORM_PREFIX)) {
                String appErrorMessage = outcome.substring(SIGNUP_FORM_PREFIX.length());
                errorMessageLabel.setText(appErrorMessage);
                showAlert(Alert.AlertType.WARNING, "Signup Failed", appErrorMessage);
            } else {
                LOGGER.log(Level.SEVERE, "Unexpected outcome from SignupController: {0}", outcome);
                showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected system error occurred during signup.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred during signup process.", e);
            errorMessageLabel.setText("An unexpected system error occurred.");
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred during signup: " + e.getMessage());
        }
    }

    private void clearErrorMessage() {
        if (userBean != null) {
            userBean.usernameErrorProperty().set("");
            userBean.passwordErrorProperty().set("");
        }
        if (errorMessageLabel != null && !errorMessageLabel.textProperty().isBound()) {
            errorMessageLabel.setText("");
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
        if (userBean != null) {
            userBean.setUsername("");
            userBean.setPassword("");
            clearErrorMessage();
        }
    }
}