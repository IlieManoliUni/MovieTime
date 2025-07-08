package ispw.project.movietime.controller.graphic.cli;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class CliController {

    @FXML
    private TextArea cliTextArea;

    @FXML
    private Button submitButton;

    private GraphicControllerCli graphicControllerCli;
    private int inputLineStart = 0;

    public CliController() {
        // Initialization logic is in initialize()
    }

    @FXML
    public void initialize() {
        this.graphicControllerCli = GraphicControllerCli.getInstance();

        cliTextArea.setWrapText(true);
        cliTextArea.setEditable(false);

        displayOutput("Welcome to Movietime CLI! Type 'help' for commands.");
        promptForInput();

        submitButton.setOnAction(event -> processCurrentInput());
        cliTextArea.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);

        cliTextArea.textProperty().addListener((obs, oldText, newText) ->
                cliTextArea.positionCaret(newText.length()));
    }

    private void displayOutput(String text) {
        cliTextArea.setEditable(true);
        cliTextArea.appendText(text + "\n");
        cliTextArea.setEditable(false);
        cliTextArea.positionCaret(cliTextArea.getText().length());
    }

    private void promptForInput() {
        displayOutput("\n> ");
        inputLineStart = cliTextArea.getText().length();
        cliTextArea.setEditable(true);
        cliTextArea.positionCaret(cliTextArea.getText().length());
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            processCurrentInput();
        } else if (event.getCode() == KeyCode.BACK_SPACE) {
            if (cliTextArea.getCaretPosition() <= inputLineStart) {
                event.consume();
            }
        } else if (event.getCode().isArrowKey()) {
            if (cliTextArea.getCaretPosition() < inputLineStart && event.getCode() != KeyCode.RIGHT) {
                cliTextArea.positionCaret(inputLineStart);
                event.consume();
            } else if (cliTextArea.getCaretPosition() > cliTextArea.getText().length() && event.getCode() == KeyCode.LEFT) {
                cliTextArea.positionCaret(cliTextArea.getText().length());
                event.consume();
            }
        } else if (cliTextArea.getCaretPosition() < inputLineStart) {
            event.consume();
        }
    }

    private void processCurrentInput() {
        cliTextArea.setEditable(false);

        String fullText = cliTextArea.getText();
        String command = "";
        if (fullText.length() > inputLineStart) {
            command = fullText.substring(inputLineStart).trim();
        }

        if (command.isEmpty()) {
            promptForInput();
            return;
        }

        switch (command.toLowerCase()) {
            case "clear":
                cliTextArea.clear();
                displayOutput("Welcome to Movietime CLI! Type 'help' for commands.");
                break;
            case "exit":
                displayOutput("Exiting application. Goodbye!");
                Platform.exit();
                return;
            default:
                try {
                    String response = graphicControllerCli.processCliCommand(command);
                    displayOutput(response);
                } catch (Exception e) {
                    displayOutput("An internal error occurred: " + e.getMessage());
                }
                break;
        }

        promptForInput();
    }
}