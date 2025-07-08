package ispw.project.movietime.controller.graphic.cli;

import ispw.project.movietime.controller.graphic.GraphicController;
import ispw.project.movietime.controller.graphic.cli.command.*;
import ispw.project.movietime.exception.UserException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphicControllerCli implements GraphicController {

    private static final Logger LOGGER = Logger.getLogger(GraphicControllerCli.class.getName());

    private static GraphicControllerCli instance;

    private final Map<String, CliCommand> commands;
    private Stage primaryStage;

    private GraphicControllerCli() {
        this.commands = new HashMap<>();
        initializeCommands();
    }

    public static synchronized GraphicControllerCli getInstance() {
        if (instance == null) {
            instance = new GraphicControllerCli();
        }
        return instance;
    }

    private void initializeCommands() {
        commands.put("help", new HelpCommand());
        commands.put("login", new LoginCommand());
        commands.put("signup", new SignUpCommand());
        commands.put("logout", new LogoutCommand());
        commands.put("createlist", new CreateListCommand());
        commands.put("deletelist", new DeleteListCommand());
        commands.put("getalllists", new GetAllListsCommand());
        commands.put("searchmovie", new SearchMovieCommand());
        commands.put("savemovietolist", new SaveMovieToListCommand());
        commands.put("deletemoviefromlist", new DeleteMovieFromListCommand());
        commands.put("seemoviedetails", new SeeMovieDetailsCommand());
        commands.put("seeallelementslist", new SeeAllElementsListCommand());
        commands.put("liststats", new ListStatsCommand());
    }

    @Override
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public String processCliCommand(String fullCommand) {
        String[] commandParts = fullCommand.split(" ", 2);
        String cmdName = commandParts[0].toLowerCase();
        String args = commandParts.length > 1 ? commandParts[1] : "";

        CliCommand command = commands.get(cmdName);

        if (command != null) {
            try {
                return command.execute(args);
            } catch (NumberFormatException e) {
                return "Error: Invalid number format for ID. Please provide a valid integer ID. Details: " + e.getMessage();
            } catch (UserException e) {
                return "User error: " + e.getMessage();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e, () -> "An unexpected error occurred during command execution: " + fullCommand);
                return "An unexpected error occurred: " + e.getMessage();
            }
        } else {
            return "Unknown command: '" + cmdName + "'. Type 'help' for a list of commands.";
        }
    }

    @Override
    public void startView() throws IOException {
        if (primaryStage == null) {
            LOGGER.severe("Primary Stage not set for GraphicControllerCli. Cannot start view.");
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ispw/project/movietime/view/cli/cli.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Movietime CLI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}