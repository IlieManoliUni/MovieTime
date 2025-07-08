package ispw.project.movietime.controller.graphic.gui;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.bean.ListBean;

import ispw.project.movietime.controller.application.ListStatsController;
import ispw.project.movietime.controller.application.ListStatsController.ListStatsResult;

import ispw.project.movietime.exception.DaoException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StatsController implements NavigableController, GraphicControllerGui.NeedsSessionUser, GraphicControllerGui.HasListBean { // ADDED HasListBean

    private static final Logger LOGGER = Logger.getLogger(StatsController.class.getName());

    private static final String SYSTEM_ERROR_TITLE = "System Error";
    private static final String NOT_LOGGED_IN_MESSAGE = "Please log in to view statistics.";
    private static final String NOT_LOADED_MESSAGE = "Stats of List (Not loaded)";
    private static final String LOGGED_OUT_MESSAGE = "Stats of List (Logged Out)";
    private static final String NO_LIST_SELECTED_MESSAGE = "No list selected to display statistics for.";


    @FXML
    private TextArea statsTextArea;

    @FXML
    private Label listNameLabel;

    @FXML
    private HBox headerBar;
    @FXML
    private DefaultController headerBarController;

    private GraphicControllerGui graphicControllerGui;

    private ObjectProperty<UserBean> sessionUserProperty;
    private ChangeListener<UserBean> sessionUserListener;

    private ListBean currentListBean;

    private ListStatsController listStatsController;

    public StatsController() {
        this.listStatsController = new ListStatsController();
    }

    @FXML
    private void initialize() {
        statsTextArea.setEditable(false);
        statsTextArea.setText("");
        listNameLabel.setText(NO_LIST_SELECTED_MESSAGE);

        sessionUserListener = (observable, oldValue, newValue) -> {
            LOGGER.log(Level.INFO, "StatsController Listener: UserBean property changed: old={0}, new={1}",
                    new Object[]{oldValue != null ? oldValue.getUsername() : "null", newValue != null ? newValue.getUsername() : "null"});
            handleSessionChange(newValue); // Unified handler for session changes
        };
    }

    @Override
    public void setGraphicController(GraphicControllerGui graphicController) {
        this.graphicControllerGui = graphicController;
        if (headerBarController != null) {
            headerBarController.setGraphicController(this.graphicControllerGui);
        } else {
            LOGGER.log(Level.WARNING, "Header bar controller is null in StatsController. The header might not function correctly.");
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

        handleSessionChange(userBeanProperty.get());
    }

    @Override
    public void setListBean(ListBean listBean) {
        this.currentListBean = listBean;
        if (this.currentListBean != null) {
            if (listNameLabel.textProperty().isBound()) {
                listNameLabel.textProperty().unbind();
            }
            listNameLabel.textProperty().bind(this.currentListBean.listNameProperty());
            LOGGER.log(Level.INFO, "StatsController received ListBean: {0}", listBean.getListName());

            if (sessionUserProperty != null && sessionUserProperty.get() != null) {
                calculateAndDisplayStats(sessionUserProperty.get());
            } else {
                prepareLoggedOutDisplay();
            }
        } else {
            listNameLabel.setText(NO_LIST_SELECTED_MESSAGE);
            statsTextArea.setText(NO_LIST_SELECTED_MESSAGE);
        }
    }

    private void handleSessionChange(UserBean currentUser) {
        if (currentUser != null) {
            LOGGER.log(Level.INFO, "StatsController: User {0} is logged in.", currentUser.getUsername());
            if (currentListBean != null) {
                LOGGER.log(Level.INFO, "StatsController: ListBean is set. Calculating stats for {0}.", currentListBean.getListName());
                calculateAndDisplayStats(currentUser);
            } else {
                LOGGER.log(Level.INFO, "StatsController: User logged in, but no ListBean set yet.");
                statsTextArea.setText(NO_LIST_SELECTED_MESSAGE);
                if (listNameLabel.textProperty().isBound()) {
                    listNameLabel.textProperty().unbind();
                }
                listNameLabel.setText(NOT_LOADED_MESSAGE);
            }
        } else {
            LOGGER.log(Level.INFO, "StatsController: User is logged out.");
            prepareLoggedOutDisplay();
        }
    }

    private void prepareLoggedOutDisplay() {
        statsTextArea.setText(NOT_LOGGED_IN_MESSAGE);
        if (listNameLabel.textProperty().isBound()) {
            listNameLabel.textProperty().unbind();
            LOGGER.log(Level.INFO, "StatsController: Unbound listNameLabel textProperty.");
        }
        listNameLabel.setText(LOGGED_OUT_MESSAGE);
    }

    private void calculateAndDisplayStats(UserBean currentUser) {
        if (graphicControllerGui == null || currentListBean == null || listStatsController == null || currentUser == null) {
            LOGGER.log(Level.SEVERE, "calculateAndDisplayStats: Initialization error. One of graphicControllerGui, currentListBean, listStatsController, or currentUser is null. " +
                            "GraphicControllerGui: {0}, CurrentListBean: {1}, ListStatsController: {2}, CurrentUser: {3}",
                    new Object[]{graphicControllerGui, currentListBean, listStatsController, currentUser});
            statsTextArea.setText("Error: Application initialization issue or no list selected.");
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "Application is not initialized correctly or no list selected. Please restart or select a list.");
            return;
        }

        try {
            ListStatsResult statsResult = listStatsController.getStatsForList(currentListBean, currentUser);

            StringBuilder details = new StringBuilder();
            details.append("Details for list '").append(statsResult.getListName()).append("':\n\n");

            if (statsResult.getNumberOfMovies() > 0) {
                details.append("--- Movies ---\n");
                details.append("Total Movies: ").append(statsResult.getNumberOfMovies()).append("\n");
                details.append("Total movie runtime: ").append(statsResult.getFormattedTotalRuntime()).append(".\n\n");
            } else {
                details.append("--- No Movies in this list ---\n\n");
            }

            details.append("Overall Total Runtime for list '").append(statsResult.getListName())
                    .append("': ").append(statsResult.getFormattedTotalRuntime()).append(".");

            statsTextArea.setText(details.toString());
            LOGGER.log(Level.INFO, "Stats calculated for list ''{0}''. Overall total runtime: {1}",
                    new Object[]{statsResult.getListName(), statsResult.getFormattedTotalRuntime()});

        } catch (DaoException e) {
            statsTextArea.setText("Error calculating stats: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Stats Error", "Could not calculate statistics for this list: " + e.getMessage());
        } catch (Exception e) {
           statsTextArea.setText("An unexpected error occurred: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, SYSTEM_ERROR_TITLE, "An unexpected error occurred while calculating statistics: " + e.getMessage());
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