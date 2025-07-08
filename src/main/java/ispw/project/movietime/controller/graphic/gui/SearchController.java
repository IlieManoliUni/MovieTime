package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.bean.MovieBean;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ispw.project.movietime.session.SessionManager;

public class SearchController implements NavigableController, GraphicControllerGui.HasSearchResults {

    private static final Logger LOGGER = Logger.getLogger(SearchController.class.getName());
    private static final String SYSTEM_ERROR_TITLE = "System Error";

    @FXML private ListView<String> listView;
    @FXML private Label searchResultsLabel;

    private ObservableList<String> items = FXCollections.observableArrayList();
    private GraphicControllerGui graphicControllerGui;

    private final Map<String, MovieBean> searchResultBeanMap = new HashMap<>();

    @FXML private HBox headerBar;
    @FXML private DefaultController headerBarController;

    @Override
    public void setGraphicController(GraphicControllerGui graphicController) {
        this.graphicControllerGui = graphicController;

        // Pass the graphicController to the included header's controller.
        if (headerBarController != null) {
            headerBarController.setGraphicController(this.graphicControllerGui);
        } else {
            LOGGER.log(Level.SEVERE, "Error: headerBarController is null in SearchController. Cannot set GraphicController for header.");
        }

        SessionManager.getInstance().currentUserBeanProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser == null) {
                items.clear();
                searchResultBeanMap.clear();
                searchResultsLabel.setText("Please log in to perform searches.");
            }
        });
    }

    @FXML
    private void initialize() {
        listView.setItems(items);
        listView.setCellFactory(param -> new CustomListCell());

        searchResultsLabel.setText("Enter a search query in the search bar.");
    }

    @Override
    public void setSearchResults(List<MovieBean> results, String searchQuery) {
        LOGGER.log(Level.INFO, "setSearchResults called. Results size: {0}, Query: {1}",
                new Object[]{results != null ? results.size() : "null", searchQuery});

        items.clear();
        searchResultBeanMap.clear();

        if (results == null || results.isEmpty()) {
            items.add("No results found for '" + searchQuery + "'.");
            searchResultsLabel.setText("No search results found for '" + searchQuery + "'.");
            return;
        }

        for (MovieBean movieBean : results) {
            String itemString = "Movie: " + movieBean.getTitle();
            String key = itemString + " (ID: " + movieBean.getId() + ")";
            items.add(key);
            searchResultBeanMap.put(key, movieBean);
        }

        searchResultsLabel.setText("Search Results for '" + searchQuery + "' (" + results.size() + " movies)");
    }

    private class CustomListCell extends ListCell<String> {
        private final HBox hbox;
        private final Text text;
        private final Button seeButton;
        private final Region spacer;

        public CustomListCell() {
            hbox = new HBox(10);
            text = new Text();
            seeButton = new Button("See Details");
            spacer = new Region();

            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox.getChildren().addAll(text, spacer, seeButton);

            setupButtonActions();
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                text.setText(item);
                setGraphic(hbox);
                // Hide button if it's the "No results found" message
                seeButton.setVisible(!item.startsWith("No results found"));
            }
        }

        private void setupButtonActions() {
            seeButton.setOnAction(event -> handleSeeDetailsAction(getItem()));
        }

        private void handleSeeDetailsAction(String itemString) {
            if (itemString == null || graphicControllerGui == null) {
                return;
            }

            MovieBean selectedMovieBean = searchResultBeanMap.get(itemString);
            if (selectedMovieBean == null) {
                showAlert(Alert.AlertType.ERROR, "Item Not Found", "Selected movie details could not be retrieved.");
                LOGGER.log(Level.WARNING, "Attempted to retrieve details for unknown itemString: {0}", itemString);
                return;
            }

            try {
                // Call the new specific navigation method in GraphicControllerGui
                graphicControllerGui.navigateToShowMovieDetails(selectedMovieBean.getId());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e, () -> "An unexpected error occurred while showing details: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred while showing details: " + e.getMessage());
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


}