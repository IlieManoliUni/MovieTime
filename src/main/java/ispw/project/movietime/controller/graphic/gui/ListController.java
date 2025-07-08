package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.SeeAllElementsListController;
import ispw.project.movietime.controller.application.DeleteMovieFromListController;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

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

public class ListController implements NavigableController, GraphicControllerGui.HasListBean {

    private static final Logger LOGGER = Logger.getLogger(ListController.class.getName());
    private static final String ID_STRING_SUFFIX = " (ID: ";
    private static final String SCREEN_LOGIN = "logIn";
    private static final String SYSTEM_ERROR_TITLE = "System Error";

    @FXML
    private ListView<String> listView;

    @FXML
    private Label listNameLabel;

    private final ObservableList<String> items = FXCollections.observableArrayList();
    private final Map<String, Object> itemBeanMap = new HashMap<>();

    private GraphicControllerGui graphicControllerGui;

    private ListBean selectedListBean;

    private final SeeAllElementsListController seeAllElementsListController;
    private final DeleteMovieFromListController deleteMovieFromListController;

    @FXML
    private HBox headerBar;
    @FXML
    private DefaultController headerBarController;

    public ListController() {
        this.seeAllElementsListController = new SeeAllElementsListController();
        this.deleteMovieFromListController = new DeleteMovieFromListController();
    }

    @FXML
    private void initialize() {
        listView.setItems(items);
        listView.setCellFactory(param -> new CustomListCell());
    }

    @Override
    public void setGraphicController(GraphicControllerGui graphicController) {
        this.graphicControllerGui = graphicController;

        if (headerBarController != null) {
            headerBarController.setGraphicController(this.graphicControllerGui);
        } else {
            LOGGER.log(Level.WARNING, "Warning: DefaultBackHomeController (headerBarController) is null in ListController. Check FXML include setup.");
        }
    }

    @Override
    public void setListBean(ListBean listBean) {
        if (this.selectedListBean != null && listNameLabel.textProperty().isBound() &&
                listNameLabel.textProperty().getBean() == this.selectedListBean) {
            listNameLabel.textProperty().unbind();
        }
        this.selectedListBean = listBean;
        listNameLabel.textProperty().bind(selectedListBean.listNameProperty());
        loadListItems();
        LOGGER.log(Level.INFO, "ListController: Set ListBean for list ''{0}''.", listBean.getListName());
    }

    private void loadListItems() {
        items.clear();
        itemBeanMap.clear();

        UserBean currentUserBean = SessionManager.getInstance().getCurrentUserBean();
        if (currentUserBean == null) {
            LOGGER.log(Level.WARNING, "loadListItems: User not logged in, cannot load items.");
            showAlert(Alert.AlertType.ERROR, "Authentication required", "Please log in to view list items.");
            return;
        }

        if (selectedListBean == null) {
            LOGGER.log(Level.WARNING, "loadListItems: No list bean selected, cannot load items.");
            return;
        }

        try {
            List<MovieBean> movieBeans = seeAllElementsListController.seeAllMoviesInList(selectedListBean.getId(), currentUserBean);

            if (movieBeans == null) {
                LOGGER.log(Level.SEVERE, "loadListItems: seeAllMoviesInList returned NULL for list ID: {0}", selectedListBean.getId());
                showAlert(Alert.AlertType.ERROR, "Loading Error", "Movie list retrieval returned an unexpected null value.");
                return; // Stop processing if the list itself is null
            }

            LOGGER.log(Level.INFO, "loadListItems: Received {0} movie beans from controller.", movieBeans.size());
            if (movieBeans.isEmpty()) {
                LOGGER.log(Level.INFO, "loadListItems: Movie list is empty. No movies to display.");
            }

            for (MovieBean movieBean : movieBeans) {
                String key = "Movie: " + movieBean.getTitle() + ID_STRING_SUFFIX + movieBean.getId() + ")";
                items.add(key);
                itemBeanMap.put(key, movieBean);
                LOGGER.log(Level.FINE, "loadListItems: Added movie to display: {0} (ID: {1})", new Object[]{movieBean.getTitle(), movieBean.getId()});
            }

            LOGGER.log(Level.INFO, "loadListItems: Finished populating ListView. Total items in list view: {0}", items.size());

        } catch (DaoException e) {
            LOGGER.log(Level.SEVERE, "Error loading list items for list ''{0}'': {1}", new Object[]{selectedListBean.getListName(), e.getMessage()});
            showAlert(Alert.AlertType.ERROR, "Error Loading List Items", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred while loading list items for list ''{0}'': {1}", new Object[]{selectedListBean.getListName(), e.getMessage()});
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred while loading list items: " + e.getMessage());
        }
    }

    private class CustomListCell extends ListCell<String> {
        private final HBox hbox;
        private final Text text;
        private final Button seeButton;
        private final Button deleteButton;
        private final Region spacer;

        public CustomListCell() {
            hbox = new HBox(10);
            text = new Text();
            seeButton = new Button("See Details");
            deleteButton = new Button("Remove");
            spacer = new Region();

            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox.getChildren().addAll(text, spacer, seeButton, deleteButton);

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
            }
        }

        private void setupButtonActions() {
            seeButton.setOnAction(event -> handleSeeAction(getItem()));
            deleteButton.setOnAction(event -> handleDeleteAction(getItem()));
        }

        private void handleSeeAction(String itemString) {
            if (itemString == null || selectedListBean == null) {
                LOGGER.log(Level.WARNING, "handleSeeAction: itemString or selectedListBean is null. Cannot proceed.");
                return;
            }

            UserBean currentUserBean = SessionManager.getInstance().getCurrentUserBean();
            if (currentUserBean == null) {
                LOGGER.log(Level.WARNING, "handleSeeAction: User not logged in, blocking action.");
                showAlert(Alert.AlertType.ERROR, "Authentication Required", "Please log in to see item details.");
                graphicControllerGui.setScreen(SCREEN_LOGIN); // Redirect to login
                return;
            }

            try {
                Object itemBean = itemBeanMap.get(itemString);
                if (itemBean == null) {
                    LOGGER.log(Level.WARNING, "handleSeeAction: Item bean not found in map for string: {0}", itemString);
                    showAlert(Alert.AlertType.ERROR, "Item Not Found", "Selected item details could not be retrieved.");
                    return;
                }

                int id;
                if (itemBean instanceof MovieBean movieBean) {
                    id = movieBean.getId();
                } else {
                    LOGGER.log(Level.WARNING, "handleSeeAction: Unsupported item type for details: {0}", itemBean.getClass().getName());
                    showAlert(Alert.AlertType.ERROR, "Unsupported Item Type", "Cannot show details for this item type (only movies supported).");
                    return;
                }
                graphicControllerGui.navigateToShowMovieDetails(id);
                LOGGER.log(Level.INFO, "Navigating to movie details for item ''{0}'', ID {1}.", new Object[]{itemString, id});

            } catch (Exception e) { // Catch any unexpected exceptions during the action
                LOGGER.log(Level.SEVERE, "An unexpected error occurred while showing details for {0}: {1}", new Object[]{itemString, e.getMessage()});
                showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred while showing details: " + e.getMessage());
            }
        }

        private void handleDeleteAction(String itemString) {
            if (itemString == null || selectedListBean == null) {
                LOGGER.log(Level.WARNING, "handleDeleteAction: itemString or selectedListBean is null. Cannot proceed.");
                return;
            }

            UserBean currentUserBean = SessionManager.getInstance().getCurrentUserBean();
            if (currentUserBean == null) {
                LOGGER.log(Level.WARNING, "handleDeleteAction: User not logged in, blocking action.");
                showAlert(Alert.AlertType.ERROR, "Authentication Required", "You must be logged in to remove items.");
                graphicControllerGui.setScreen(SCREEN_LOGIN); // Redirect to login
                return;
            }

            try {
                Object itemBean = itemBeanMap.get(itemString);
                if (itemBean == null) {
                    LOGGER.log(Level.WARNING, "handleDeleteAction: Item bean not found in map for string: {0}", itemString);
                    showAlert(Alert.AlertType.ERROR, "Item Not Found", "Selected item could not be removed.");
                    return;
                }

                boolean removed = false;
                if (itemBean instanceof MovieBean movieBean) {
                    removed = deleteMovieFromListController.deleteMovieFromList(
                            selectedListBean.getId(), movieBean.getId(), currentUserBean);
                    LOGGER.log(Level.INFO, "Attempted to remove Movie ''{0}'' from list ''{1}''. Result: {2}",
                            new Object[]{itemString, selectedListBean.getListName(), removed ? "Success" : "Not Found/Failed"});
                } else {
                    LOGGER.log(Level.WARNING, "handleDeleteAction: Unsupported item type for removal: {0}", itemBean.getClass().getName());
                    showAlert(Alert.AlertType.WARNING, "Feature Not Available", "Only movie removal is supported at this time.");
                    return;
                }

                if (removed) {
                    getListView().getItems().remove(itemString); // Remove from ObservableList
                    itemBeanMap.remove(itemString); // Remove from map
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Item removed from list.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Removal Info", "Item was not removed. It might not be in the list, or the operation failed.");
                }

            } catch (DaoException e) {
                LOGGER.log(Level.SEVERE, "Removal failed for item {0} from list ''{1}'': {2}", new Object[]{itemString, selectedListBean.getListName(), e.getMessage()});
                showAlert(Alert.AlertType.ERROR, "Removal Failed", e.getMessage());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "An unexpected error occurred during removal of item {0} from list ''{1}'': {2}", new Object[]{itemString, selectedListBean.getListName(), e.getMessage()});
                showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred during removal: " + e.getMessage());
            }
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