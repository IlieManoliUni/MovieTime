package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.controller.application.SaveMovieToListController;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ShowController implements NavigableController, GraphicControllerGui.NeedsSessionUser {

    private static final Logger LOGGER = Logger.getLogger(ShowController.class.getName());
    private static final String TITLE_PREFIX = "Title: ";
    private static final String SYSTEM_ERROR_TITLE = "System Error";

    @FXML
    private ImageView photoView;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField listNameField;

    @FXML
    private Button addToListButton;

    @FXML
    private HBox addToListContainer;

    @FXML
    private HBox headerBar;

    @FXML
    private DefaultController headerBarController;

    private GraphicControllerGui graphicControllerGui;

    private ChangeListener<UserBean> sessionUserListener;
    private ObjectProperty<UserBean> sessionUserProperty;

    private MovieBean currentMovieBean;
    private int currentId;

    // Injected Use Case Controllers
    private final SaveMovieToListController saveMovieToListController;

    public ShowController() {
        this.saveMovieToListController = new SaveMovieToListController();
    }

    @FXML
    private void initialize() {
        descriptionArea.setWrapText(true);
        if (addToListContainer != null) {
            addToListContainer.setVisible(false);
            addToListContainer.setManaged(false);
        }
        sessionUserListener = (observable, oldValue, newValue) -> toggleAddToListControls(newValue != null);
    }

    @Override
    public void setGraphicController(GraphicControllerGui graphicController) {
        this.graphicControllerGui = graphicController;

        if (headerBarController != null) {
            headerBarController.setGraphicController(this.graphicControllerGui);
        } else {
            LOGGER.log(Level.WARNING, "Header bar controller is null in ShowController. The header might not function correctly.");
        }
    }

    public void setItemDetails(int id, MovieBean movieBean) {
        this.currentId = id;
        this.currentMovieBean = movieBean;

        if (this.currentMovieBean != null) {
            populateDetailsInUI();

            toggleAddToListControls(sessionUserProperty != null && sessionUserProperty.get() != null);
        } else {
            showAlert(Alert.AlertType.WARNING, "Movie Not Found", "Details for the selected movie could not be retrieved. Redirecting to home.");
            if (graphicControllerGui != null) {
                graphicControllerGui.setScreen("home");
            }
        }
    }

    @Override
    public void setSessionUserProperty(ObjectProperty<UserBean> userBeanProperty) {
        if (this.sessionUserProperty != null) {
            this.sessionUserProperty.removeListener(sessionUserListener);
        }
        this.sessionUserProperty = userBeanProperty;
        this.sessionUserProperty.addListener(sessionUserListener);

        if (headerBarController != null && headerBarController instanceof GraphicControllerGui.NeedsSessionUser headerNeedsSession) {
            headerNeedsSession.setSessionUserProperty(userBeanProperty);
        }

        toggleAddToListControls(userBeanProperty.get() != null);
    }

    private void toggleAddToListControls(boolean visible) {
        if (addToListContainer != null) {
            addToListContainer.setVisible(visible);
            addToListContainer.setManaged(visible);
        }
    }

    private void populateDetailsInUI() {
        if (currentMovieBean == null) {
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "No movie details to display.");
            return;
        }

        StringBuilder detailsText = new StringBuilder();
        String imageUrl = null;

        detailsText.append(TITLE_PREFIX).append(currentMovieBean.getTitle()).append("\n");
        detailsText.append("Overview: ").append(currentMovieBean.getOverview()).append("\n\n");
        detailsText.append("Original Title: ").append(currentMovieBean.getOriginalTitle()).append("\n");
        detailsText.append("Original Language: ").append(currentMovieBean.getOriginalLanguage()).append("\n");
        detailsText.append("Release Date: ").append(currentMovieBean.getFormattedReleaseDate()).append("\n");
        detailsText.append("Runtime: ").append(currentMovieBean.getRuntimeDisplay()).append("\n");
        detailsText.append("Genres: ").append(currentMovieBean.getGenresDisplay()).append("\n");
        detailsText.append("Vote Average: ").append(currentMovieBean.getVoteAverageDisplay()).append("\n");
        detailsText.append("Budget: ").append(currentMovieBean.getBudgetDisplay()).append("\n");
        detailsText.append("Revenue: ").append(currentMovieBean.getRevenueDisplay()).append("\n");
        detailsText.append("Production Companies: ").append(currentMovieBean.getProductionCompaniesDisplay()).append("\n");
        detailsText.append("Production Countries: ").append(currentMovieBean.getProductionCountriesDisplay()).append("\n");
        detailsText.append("Spoken Languages: ").append(currentMovieBean.getSpokenLanguagesDisplay()).append("\n");

        imageUrl = currentMovieBean.getFullPosterUrl();

        descriptionArea.setText(detailsText.toString());

        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.contains("default_no_poster.png")) {
            try {
                Image image = new Image(imageUrl);
                photoView.setImage(image);
            } catch (IllegalArgumentException e) {
                String finalImageUrl = imageUrl;
                LOGGER.log(Level.WARNING, e, () -> "Failed to load image from URL: " + finalImageUrl);
                photoView.setImage(null);
            }
        } else {
            photoView.setImage(null);
        }
    }

    @FXML
    private void addToUserList() {
        if (saveMovieToListController == null) {
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "Application controllers are not initialized correctly.");
            return;
        }

        UserBean currentUserBean = SessionManager.getInstance().getCurrentUserBean();
        if (currentUserBean == null) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In", "Please log in to add items to a list.");
            toggleAddToListControls(false);
            return;
        }

        try {
            String listName = listNameField.getText().trim();
            if (listName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a list name.");
                return;
            }

            ListBean targetListBean = saveMovieToListController.findListForUserByName(currentUserBean, listName);

            if (targetListBean == null) {
                showAlert(Alert.AlertType.ERROR, "List Not Found", "List '" + listName + "' not found for current user. Please create it first.");
                return;
            }

            boolean success = false;
            if (currentMovieBean != null) {
                success = saveMovieToListController.saveMovieToList(targetListBean, currentMovieBean, currentUserBean);
            } else {
                showAlert(Alert.AlertType.ERROR, "No Movie Selected", "Cannot add to list, no movie details are displayed.");
                return;
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Movie added to list '" + listName + "'.");
                listNameField.clear();
            } else {
                showAlert(Alert.AlertType.WARNING, "Add Failed", "Could not add movie to list. It might already be there.");
            }

        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error Adding to List", e.getMessage());
            LOGGER.log(Level.WARNING, "Application error adding to list: {0}", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, () -> "Unexpected error in addToUserList for movie ID: " + currentId);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}