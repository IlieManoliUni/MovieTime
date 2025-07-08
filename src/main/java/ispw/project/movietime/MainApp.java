package ispw.project.movietime;

import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.controller.graphic.GraphicController;
import ispw.project.movietime.controller.graphic.cli.GraphicControllerCli;
import ispw.project.movietime.controller.graphic.gui.GraphicControllerGui;

import ispw.project.movietime.dao.DaoType;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    private static final LaunchType DESIRED_LAUNCH_TYPE = LaunchType.GUI;
    private static final DaoType DESIRED_PERSISTENCE_TYPE = DaoType.JDBC;

    private static LaunchType currentLaunchType;
    private static ApplicationConfig appConfig;

    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "Application main method started.");

        appConfig = new ApplicationConfig();
        currentLaunchType = DESIRED_LAUNCH_TYPE;

        try {
            ApplicationControllerProvider.initialize(DESIRED_PERSISTENCE_TYPE);
            LOGGER.log(Level.INFO, "ApplicationControllerProvider initialized successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize ApplicationControllerProvider: {0}", e.getMessage());
            System.exit(1);
        }

        Application.launch(MainApp.class, args);

        LOGGER.log(Level.INFO, "Application main method finished.");
    }

    @Override
    public void start(Stage primaryStage) {
        LOGGER.log(Level.INFO, "JavaFX Application starting...");
        try {
            GraphicController controller;
            String guiFxmlPath = appConfig.getProperty("gui.fxml.path.prefix", "/ispw/project/movietime/view/gui/");

            if (currentLaunchType == LaunchType.GUI) {
                controller = GraphicControllerGui.getInstance(guiFxmlPath);
            } else {
                controller = GraphicControllerCli.getInstance();
            }

            controller.setPrimaryStage(primaryStage);
            controller.startView();

            LOGGER.log(Level.INFO, "JavaFX Application started successfully.");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading view during application startup: {0}", e.getMessage());
            System.exit(1);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Application initialization error: {0}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error during application startup.", e);
            System.exit(1);
        }
    }
}