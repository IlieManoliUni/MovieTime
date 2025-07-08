package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.controller.graphic.GraphicController;
import ispw.project.movietime.session.SessionManager;
import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.controller.application.SeeMovieDetailsController;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ispw.project.movietime.exception.ApiException;

public class GraphicControllerGui implements GraphicController {

    private static final Logger LOGGER = Logger.getLogger(GraphicControllerGui.class.getName());
    private static final String SYSTEM_ERROR_TITLE = "System Error";
    private static final String LOGINSCREEN = "logIn";

    private static GraphicControllerGui instance;

    private final Map<String, String> screenPaths = new HashMap<>();
    private final Deque<ScreenHistoryEntry> screenHistory = new ArrayDeque<>();
    private Stage primaryStage;

    private final String fxmlPathPrefix;

    // --- Caches for screen-specific data ---
    private DefaultController.SearchResultData lastSearchResults;
    // --- END NEW ---

    private GraphicControllerGui(String fxmlPathPrefix) {
        this.fxmlPathPrefix = fxmlPathPrefix;

        addScreen(LOGINSCREEN, this.fxmlPathPrefix + "logIn.fxml");
        addScreen("home", this.fxmlPathPrefix + "home.fxml");
        addScreen("list", this.fxmlPathPrefix + "list.fxml");
        addScreen("search", this.fxmlPathPrefix + "search.fxml");
        addScreen("show", this.fxmlPathPrefix + "show.fxml");
        addScreen("signIn", this.fxmlPathPrefix + "signIn.fxml");
        addScreen("stats", this.fxmlPathPrefix + "stats.fxml");
    }

    public static synchronized GraphicControllerGui getInstance(String fxmlPathPrefix) {
        if (instance == null) {
            instance = new GraphicControllerGui(fxmlPathPrefix);
        }
        return instance;
    }

    @Override
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void addScreen(String name, String fxmlPath) {
        screenPaths.put(name, fxmlPath);
    }

    /**
     * Sets the primary stage's scene to a new FXML screen without passing any data.
     * This is typically used for simple navigations like "home" or "back".
     * @param name The registered name of the screen (e.g., "home", "logIn").
     */
    public void setScreen(String name) {
        setScreen(name, null, false); // Default to not from back navigation
    }

    /**
     * Sets the primary stage's scene to a new FXML screen, optionally passing data to its controller.
     * @param name The registered name of the screen.
     * @param data Optional data to pass to the controller.
     */
    public void setScreen(String name, Object data) {
        setScreen(name, data, false); // Default to not from back navigation
    }

    /**
     * Sets the primary stage's scene to a new FXML screen, optionally passing data to its controller.
     *
     * @param name The registered name of the screen (e.g., "search", "show").
     * @param data Optional data to pass to the controller. This can be SearchResultData, MovieDetailsData, or ListBean.
     * @param fromBackNavigation A flag to indicate if this call originated from the 'goBack()' method.
     * This is crucial for correct history management.
     */
    public void setScreen(String name, Object data, boolean fromBackNavigation) {
        if (!screenPaths.containsKey(name)) {
            showAlert(Alert.AlertType.ERROR, "Navigation screen error", "Screen not registered: " + name);
            LOGGER.log(Level.SEVERE, "Attempted to set unregistered screen: {0}", name);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screenPaths.get(name)));
            Parent root = loader.load();
            NavigableController controller = loader.getController();

            // Validate and initialize controller
            validateController(controller, name);
            controller.setGraphicController(this);
            // Refactored: Pattern matching for instanceof
            if (controller instanceof NeedsSessionUser needsSessionController) {
                needsSessionController.setSessionUserProperty(SessionManager.getInstance().currentUserBeanProperty());
            }

            // Handle data passing to the controller and caching
            handleScreenData(name, data, controller);

            // Update the primary stage's scene
            updatePrimaryStageScene(root);

            // Manage navigation history
            manageHistory(name, data, fromBackNavigation);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation screen error", "Could not load screen: " + name + ".\n" + e.getMessage());
            LOGGER.log(Level.SEVERE, e, () -> "Failed to load FXML for screen: " + name);
        } catch (Exception e) { // Catch-all for any other unexpected runtime exceptions
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred during screen transition: " + e.getMessage());
            LOGGER.log(Level.SEVERE, e, () -> "An unexpected error occurred during screen transition to " + name);
        }
    }

    // --- Helper methods for setScreen to reduce complexity ---

    private void validateController(NavigableController controller, String screenName) throws IOException {
        if (controller == null) {
            LOGGER.log(Level.SEVERE, "FXMLLoader did not provide a controller for {0}.fxml", screenName);
            throw new IOException("Controller not found for FXML: " + screenName);
        }
    }

    private void handleScreenData(String screenName, Object data, NavigableController controller) {
        if (data instanceof DefaultController.SearchResultData searchResultData) {
            applySearchResults(controller, searchResultData);
        } else if (screenName.equals("search") && this.lastSearchResults != null) {
            restoreCachedSearchResults(controller);
        } else if (screenName.equals("show") && data instanceof MovieDetailsData movieDetailsData) {
            applyMovieDetails(controller, movieDetailsData);
        } else if (data instanceof ListBean listBeanData) {
            applyListBeanData(controller, listBeanData);
        } else if (data != null) {
            LOGGER.log(Level.WARNING, "Controller for screen received an unexpected data type. Data might not be processed correctly for this screen.");
        }
    }

    private void applySearchResults(NavigableController controller, DefaultController.SearchResultData searchResultData) {
        if (controller instanceof HasSearchResults hasResultsController) {
            hasResultsController.setSearchResults(searchResultData.getResults(), searchResultData.getQuery());
            this.lastSearchResults = searchResultData;
        } else {
            LOGGER.log(Level.WARNING, "Controller {0} received SearchResultData but does not implement HasSearchResults. Data might not be displayed.", controller.getClass().getName());
        }
    }

    private void restoreCachedSearchResults(NavigableController controller) {
        if (controller instanceof HasSearchResults hasResultsController) {
            hasResultsController.setSearchResults(this.lastSearchResults.getResults(), this.lastSearchResults.getQuery());
            LOGGER.log(Level.INFO, "Restored previous search results for screen.");
        } else {
            LOGGER.log(Level.WARNING, "Search screen controller does not implement HasSearchResults. Cached data will not be displayed.");
        }
    }

    private void applyMovieDetails(NavigableController controller, MovieDetailsData movieDetailsData) {
        if (controller instanceof ShowController showController) {
            showController.setItemDetails(movieDetailsData.movieId(), movieDetailsData.movieBean());
        } else {
            LOGGER.log(Level.WARNING, "Show screen controller received MovieDetailsData but is not ShowController. Data might not be displayed.");
        }
    }

    private void applyListBeanData(NavigableController controller, ListBean listBeanData) {
        if (controller instanceof HasListBean hasListBeanController) {
            hasListBeanController.setListBean(listBeanData);
        } else {
            LOGGER.log(Level.WARNING, "Controller for screen received ListBean but does not implement HasListBean. List data might not be restored.");
        }
    }

    private void updatePrimaryStageScene(Parent root) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.show();
    }

    private void manageHistory(String screenName, Object data, boolean fromBackNavigation) {
        if (!fromBackNavigation && (screenHistory.isEmpty() || !screenHistory.peekLast().name().equals(screenName) || data != null)) {
            screenHistory.offerLast(new ScreenHistoryEntry(screenName, data));
        }
    }

    // --- END Helper methods for setScreen ---


    /**
     * Navigates to a specific list detail screen (e.g., "list" or "stats")
     * and passes the selected ListBean to its controller.
     *
     * @param selectedListBean The ListBean representing the list to display details for.
     * @param screenName The registered name of the target screen (e.g., "list", "stats").
     */
    public void navigateToListDetail(ListBean selectedListBean, String screenName) {
        if (!screenPaths.containsKey(screenName)) {
            showAlert(Alert.AlertType.ERROR, "Navigation error", "Target screen '" + screenName + "' not registered for list details.");
            LOGGER.log(Level.SEVERE, "Attempted to navigate to unregistered screen: {0} with list data.", screenName);
            return;
        }
        if (selectedListBean == null) {
            showAlert(Alert.AlertType.ERROR, "Navigation error", "No list selected for details.");
            LOGGER.log(Level.WARNING, "Attempted to navigate to list details with a null ListBean.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screenPaths.get(screenName)));
            Parent root = loader.load();
            NavigableController controller = loader.getController();

            if (controller == null) {
                LOGGER.log(Level.SEVERE, "FXMLLoader did not provide a controller for {0}.fxml", screenName);
                throw new IOException("Controller not found for FXML: " + screenName);
            }

            controller.setGraphicController(this);

            // Refactored: Pattern matching for instanceof
            if (controller instanceof NeedsSessionUser needsSessionController) {
                needsSessionController.setSessionUserProperty(SessionManager.getInstance().currentUserBeanProperty());
            }

            if (controller instanceof HasListBean hasListBeanController) {
                hasListBeanController.setListBean(selectedListBean);
            } else {
                LOGGER.log(Level.WARNING, "Controller for {0} does not implement HasListBean. List data might not be passed correctly.", screenName);
                showAlert(Alert.AlertType.WARNING, "Warning", "Cannot pass list data to the target screen. Controller does not support it.");
            }

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            primaryStage.show();

            // Always treat navigateToListDetail as a forward navigation (not from back)
            // This will correctly add it to history via the setScreen method's history logic.
            // The previous history management code here is now redundant with the new setScreen signature
            // Instead, we just ensure setScreen handles it by calling the 3-arg version.
            setScreen(screenName, selectedListBean, false); // Call the new setScreen to manage history

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error ", "Could not load screen: " + screenName + " for list details.\n" + e.getMessage());
            LOGGER.log(Level.SEVERE, e, () -> "Failed to load FXML for screen: " + screenName + " with list data.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred during list detail screen transition: " + e.getMessage());
            LOGGER.log(Level.SEVERE, e, () -> "An unexpected error occurred during list detail screen transition to " + screenName);
        }
    }

    /**
     * Navigates to the "show" screen to display detailed information for a specific movie.
     * This method fetches the full MovieBean via the application layer and passes it to the ShowController.
     *
     * @param movieId The ID of the movie whose details are to be displayed.
     */
    public void navigateToShowMovieDetails(int movieId) {
        if (!screenPaths.containsKey("show")) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Show screen not registered.");
            LOGGER.log(Level.SEVERE, "Attempted to navigate to unregistered show screen.");
            return;
        }

        try {
            SeeMovieDetailsController movieDetailsAppController = new SeeMovieDetailsController();
            MovieBean movieDetailsBean = movieDetailsAppController.seeMovieDetails(movieId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(screenPaths.get("show")));
            Parent root = loader.load();
            NavigableController controller = loader.getController();

            if (controller == null) {
                LOGGER.log(Level.SEVERE, "FXMLLoader did not provide a controller for show.fxml");
                throw new IOException("Controller not found for FXML: show");
            }

            controller.setGraphicController(this);

            if (controller instanceof NeedsSessionUser needsSessionController) {
                needsSessionController.setSessionUserProperty(SessionManager.getInstance().currentUserBeanProperty());
            }

            if (controller instanceof ShowController showController) {
                showController.setItemDetails(movieId, movieDetailsBean);
                LOGGER.log(Level.INFO, "Navigated to show screen for movie ID: {0}, Title: {1}",
                        new Object[]{movieId, movieDetailsBean != null ? movieDetailsBean.getTitle() : "N/A"});
            } else {
                LOGGER.log(Level.WARNING, "Show screen controller is not an instance of ShowController. Movie details might not be displayed.");
                showAlert(Alert.AlertType.WARNING, "Display Error", "Cannot display movie details. Controller type mismatch.");
            }

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            primaryStage.show();

            // Always treat navigateToShowMovieDetails as a forward navigation (not from back)
            // This ensures history is correctly managed via the setScreen method's history logic.
            // The previous history management code here is now redundant.
            setScreen("show", new MovieDetailsData(movieId, movieDetailsBean), false); // Call the new setScreen to manage history

        } catch (ApiException e) {
            showAlert(Alert.AlertType.ERROR, "Movie Details Error", "Could not retrieve movie details from API: " + e.getMessage());
            LOGGER.log(Level.SEVERE, e, () -> "Failed to get movie details from API for ID: " + movieId);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load show screen.\n" + e.getMessage());
            LOGGER.log(Level.SEVERE, e, () -> "Failed to load FXML for show screen.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred during show screen transition: " + e.getMessage());
            LOGGER.log(Level.SEVERE, e, () -> "An unexpected error occurred during show screen transition to show screen.");
        }
    }


    /**
     * Placeholder method; generally, GraphicControllerGui shouldn't directly provide
     * controllers as they are loaded per request.
     */
    public NavigableController getController(String screenName) {
        LOGGER.log(Level.WARNING, "Attempted to get controller for screen {0}. GraphicControllerGui is configured to load new controllers per screen set, so this will return null.", screenName);
        return null;
    }

    /**
     * Navigates back to the previous screen in the history.
     */
    public void goBack() {
        if (screenHistory.size() > 1) {
            // Remove the current screen from history
            screenHistory.pollLast();
            // Get the previous screen entry (which is now the current top of the stack)
            ScreenHistoryEntry previousEntry = screenHistory.peekLast();

            // Pass the stored data back to setScreen, and crucially, indicate it's a back navigation
            setScreen(previousEntry.name(), previousEntry.data(), true); // Pass 'true' for fromBackNavigation
        } else {
            LOGGER.warning("Attempted to go back with no screen history (only current screen remaining).");
            // If at the base screen (e.g., home or login), optionally disable back button or show a message.
            // For now, it will simply do nothing.
        }
    }

    /**
     * Helper method to display an alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void startView() {
        if (primaryStage == null) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Primary Stage not set for GraphicControllerGui.");
            LOGGER.log(Level.SEVERE, "Primary Stage is null during startView.");
            return;
        }

        if (SessionManager.getInstance().isLoggedIn()) {
            setScreen("home", null, false); // Initial screen, not from back navigation
        } else {
            setScreen(LOGINSCREEN, null, false); // Initial screen, not from back navigation
        }

        primaryStage.setTitle("Movietime GUI");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.setResizable(true);
    }


    /**
     * Interface for GUI controllers that need to receive the session UserBean property.
     * Implementing controllers should add a listener to this property to react to login/logout changes.
     */
    public interface NeedsSessionUser {
        void setSessionUserProperty(ObjectProperty<UserBean> userBeanProperty);
    }

    /**
     * Interface for GUI controllers that display search results (e.g., 'search' screen).
     */
    public interface HasSearchResults {
        /**
         * Sets the search results and the original search query for this controller to display.
         * @param results A list of MovieBean objects representing the search results.
         * @param searchQuery The original query string used for the search.
         */
        void setSearchResults(List<MovieBean> results, String searchQuery);
    }

    /**
     * Interface for GUI controllers that need to receive a ListBean
     * to display details about a specific movie list (e.g., list content, stats).
     */
    public interface HasListBean {
        /**
         * Sets the ListBean for this controller to display or process.
         * @param listBean The ListBean containing the details of the selected movie list.
         */
        void setListBean(ListBean listBean);
    }

    /**
     * Helper record to bundle movie details data for the 'show' screen.
     */
    public record MovieDetailsData(int movieId, MovieBean movieBean) {}

    /**
     * Helper record to store screen name and its associated data in the history stack.
     */
    private record ScreenHistoryEntry(String name, Object data) {}
}