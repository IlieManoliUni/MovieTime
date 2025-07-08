package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.UserBean;

// Importing all necessary application controllers
import ispw.project.movietime.controller.application.GetAllListsController;
import ispw.project.movietime.controller.application.CreateListController;
import ispw.project.movietime.controller.application.DeleteListController; // Import DeleteListController

import ispw.project.movietime.exception.DaoException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeController implements NavigableController, GraphicControllerGui.NeedsSessionUser {

    private static final Logger LOGGER = Logger.getLogger(HomeController.class.getName());
    private static final String SCREEN_LOGIN = "logIn";
    private static final String SYSTEM_ERROR_TITLE = "System Error";

    private ChangeListener<UserBean> sessionUserListener;

    @FXML
    private ListView<ListBean> listView;

    @FXML
    private TextField textField;

    @FXML
    private Button createButton;

    private final ObservableList<ListBean> items = FXCollections.observableArrayList();

    private GraphicControllerGui graphicControllerGui;
    private ObjectProperty<UserBean> sessionUserProperty; // Holds the UserBean for the current session

    @FXML
    private HBox headerInclude;

    @FXML
    private DefaultController headerIncludeController;

    // Application controllers - injected via constructor
    private GetAllListsController getAllListsController;
    private CreateListController createListController;
    private DeleteListController deleteListController; // New: Inject DeleteListController

    // Updated Constructor for dependency injection of all necessary application controllers
    public HomeController() {
        this.getAllListsController = new GetAllListsController();
        this.createListController = new CreateListController();
        this.deleteListController = new DeleteListController(); // Initialize DeleteListController
    }

    @FXML
    private void initialize() {
        listView.setItems(items);
        listView.setCellFactory(param -> new CustomListCell()); // CustomListCell uses ListBean

        if (createButton != null) {
            createButton.setOnAction(event -> handleCreateButton());
        }

        // Initialize the listener once
        sessionUserListener = (observable, oldValue, newValue) -> {
            LOGGER.log(Level.INFO, "HomeController Listener: sessionUserProperty changed: old={0}, new={1}", new Object[]{oldValue, newValue});
            if (newValue != null) { // User is now logged in (newValue is not null)
                LOGGER.log(Level.INFO, "HomeController Listener: User is now logged in, calling loadUserLists.");
                loadUserLists(newValue); // Pass the current UserBean
            } else { // User is logged out (newValue is null)
                LOGGER.log(Level.INFO, "HomeController Listener: User is NOT logged in, clearing lists.");
                items.clear();
            }
        };
    }

    @Override
    public void setGraphicController(GraphicControllerGui graphicController) {
        this.graphicControllerGui = graphicController;

        if (headerIncludeController != null) {
            headerIncludeController.setGraphicController(this.graphicControllerGui);
        }
    }

    @Override
    public void setSessionUserProperty(ObjectProperty<UserBean> userBeanProperty) {
        // Remove old listener if it exists to prevent duplicates
        if (this.sessionUserProperty != null && sessionUserListener != null) {
            this.sessionUserProperty.removeListener(sessionUserListener);
        }

        this.sessionUserProperty = userBeanProperty;
        // Add the listener to the new property
        if (sessionUserListener != null) {
            this.sessionUserProperty.addListener(sessionUserListener);
        } else {
            LOGGER.log(Level.SEVERE, "Session user listener was null during setSessionUserProperty.");
        }

        // Also pass the user property to the header if it needs it
        // This assumes DefaultController also implements GraphicControllerGui.NeedsSessionUser
        if (headerIncludeController != null && headerIncludeController instanceof GraphicControllerGui.NeedsSessionUser headerNeedsSession) {
            headerNeedsSession.setSessionUserProperty(userBeanProperty);
        }

        // Initial load based on current login state
        if (userBeanProperty.get() != null) {
            LOGGER.log(Level.INFO, "HomeController.setSessionUserProperty(): Initial check: User IS logged in. Calling loadUserLists.");
            loadUserLists(userBeanProperty.get());
        } else {
            LOGGER.log(Level.INFO, "HomeController.setSessionUserProperty(): Initial check: User IS NOT logged in. Clearing lists.");
            items.clear();
        }
    }

    private void loadUserLists(UserBean currentUser) {
        items.clear();

        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "loadUserLists: Current UserBean is null, cannot load lists.");
            return;
        }

        try {
            // DELEGATE: Call the application layer controller
            List<ListBean> lists = getAllListsController.getAllListsForUser(currentUser);

            items.setAll(lists);
            LOGGER.log(Level.INFO, "loadUserLists: Successfully loaded {0} lists.", items.size());
        } catch (DaoException e) {
            LOGGER.log(Level.SEVERE, "Error loading lists: {0}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error Loading Lists", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred while loading lists: {0}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred while loading lists: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateButton() {
        String newItemName = textField.getText().trim();
        if (newItemName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a valid list name.");
            return;
        }

        UserBean currentUser = sessionUserProperty.get();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error Log", "You must be logged in to create a list.");
            graphicControllerGui.setScreen(SCREEN_LOGIN);
            return;
        }

        try {
            // DELEGATE: Call the application layer controller
            ListBean newlyCreatedList = createListController.createNewList(newItemName, currentUser);
            textField.clear();
            items.add(newlyCreatedList); // Add the newly created list directly to the observable list
            showAlert(Alert.AlertType.INFORMATION, "Success", "List '" + newlyCreatedList.getListName() + "' created.");
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "List Creation Failed", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred while creating the list: {0}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred while creating the list: " + e.getMessage());
        }
    }


    // CustomListCell now works with ListBean
    private class CustomListCell extends ListCell<ListBean> {
        private final HBox hbox;
        private final Text text;
        private final Button seeButton;
        private final Button statsButton;
        private final Button deleteButton;
        private final Region spacer;

        public CustomListCell() {
            hbox = new HBox(10);
            text = new Text();
            seeButton = new Button("See");
            statsButton = new Button("Stats");
            deleteButton = new Button("Delete");
            spacer = new Region();

            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox.getChildren().addAll(text, spacer, seeButton, statsButton, deleteButton);

            setupButtonActions();
        }

        @Override
        protected void updateItem(ListBean item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                // CORRECTED: Use listNameProperty() as defined in ListBean
                text.textProperty().bind(item.listNameProperty());
                setGraphic(hbox);
            }
        }

        private void setupButtonActions() {
            // Pass the ListBean directly to navigation/deletion methods
            seeButton.setOnAction(event -> navigateToScreen(getItem(), "list"));
            statsButton.setOnAction(event -> navigateToScreen(getItem(), "stats"));
            deleteButton.setOnAction(event -> handleDeleteAction(getItem())); // Changed to pass ListBean directly
        }

        private void navigateToScreen(ListBean selectedListBean, String screen) {
            if (selectedListBean == null) return;

            UserBean currentUser = sessionUserProperty.get();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "You must be logged in to view lists.");
                graphicControllerGui.setScreen(SCREEN_LOGIN);
                return;
            }

            try {
                // Pass the ListBean directly to the graphic controller for navigation
                graphicControllerGui.navigateToListDetail(selectedListBean, screen);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "An unexpected error occurred during navigation: {0}", e.getMessage());
                showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred during navigation: " + e.getMessage());
            }
        }

        private void handleDeleteAction(ListBean listToDeleteBean) {
            if (listToDeleteBean == null) return;

            UserBean currentUser = sessionUserProperty.get();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "You must be logged in to delete lists.");
                graphicControllerGui.setScreen(SCREEN_LOGIN);
                return;
            }

            // Optional: Confirmation dialog before deletion
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Delete List: " + listToDeleteBean.getListName() + "?");
            confirmationAlert.setContentText("Are you sure you want to delete this list? This action cannot be undone.");
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // DELEGATE: Call the application layer's DeleteListController
                        deleteListController.deleteList(listToDeleteBean, currentUser); // Direct call to injected controller

                        items.remove(listToDeleteBean); // Remove the bean from the observable list
                        showAlert(Alert.AlertType.INFORMATION, "Success", "List '" + listToDeleteBean.getListName() + "' deleted.");
                    } catch (DaoException e) {
                        showAlert(Alert.AlertType.ERROR, "Deletion Failed", e.getMessage());
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred while deleting the list: " + e.getMessage());
                    }
                }
            });
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