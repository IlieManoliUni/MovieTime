package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.controller.application.SearchMovieController;
import ispw.project.movietime.exception.ApiException;
import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.bean.MovieSearchBean;
import ispw.project.movietime.session.SessionManager;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

public class DefaultController implements NavigableController {

    @FXML private Button backButton;
    @FXML private Button homeButton;
    @FXML private Button searchButton;
    @FXML private Button userButton;
    @FXML private TextField searchBar;
    @FXML private Label searchErrorLabel;

    private GraphicControllerGui graphicControllerGui;

    private MovieSearchBean movieSearchBean;

    private static final String SEARCH_SCREEN_NAME = "search";

    @Override
    public void setGraphicController(GraphicControllerGui graphicController) {
        this.graphicControllerGui = graphicController;
    }

    @FXML
    private void initialize() {
        movieSearchBean = new MovieSearchBean();

        searchBar.textProperty().bindBidirectional(movieSearchBean.searchQueryProperty());

        searchErrorLabel.textProperty().bind(movieSearchBean.searchErrorProperty());
        searchErrorLabel.getStyleClass().add("error-label");

        StringBinding userButtonTextBinding = Bindings.createStringBinding(() -> {
            if (SessionManager.getInstance().isLoggedIn()) {
                return SessionManager.getInstance().getCurrentUserBean().getUsername();
            } else {
                return "Log In";
            }
        }, SessionManager.getInstance().currentUserBeanProperty());

        userButton.textProperty().bind(userButtonTextBinding);
    }

    @FXML
    private void handleBackButtonAction() {
        graphicControllerGui.goBack();
    }

    @FXML
    private void handleHomeButtonAction() {
        graphicControllerGui.setScreen("home");
    }

    @FXML
    private void handleSearchButtonAction() {
        if (!movieSearchBean.isValid()) {
            return;
        }

        String searchText = movieSearchBean.getSearchQuery();

        SearchMovieController searchMovieController = new SearchMovieController();
        try {
            List<MovieBean> searchResults = searchMovieController.searchMovies(searchText);
            graphicControllerGui.setScreen(SEARCH_SCREEN_NAME, new SearchResultData(searchResults, searchText));

        } catch (ApiException e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", "Failed to search movies: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "System Error", "An unexpected error occurred during search: " + e.getMessage());
        }
    }

    @FXML
    private void handleUserButtonAction() {
        if (SessionManager.getInstance().isLoggedIn()) {
            SessionManager.getInstance().logout();
            graphicControllerGui.setScreen("logIn");
        } else {
            graphicControllerGui.setScreen("logIn");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class SearchResultData {
        private final List<MovieBean> results;
        private final String query;

        public SearchResultData(List<MovieBean> results, String query) {
            this.results = results;
            this.query = query;
        }

        public List<MovieBean> getResults() {
            return results;
        }

        public String getQuery() {
            return query;
        }
    }
}