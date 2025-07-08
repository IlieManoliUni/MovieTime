package ispw.project.movietime.controller.graphic;

import javafx.stage.Stage;

import java.io.IOException;

public interface GraphicController {
    public void startView() throws IOException;

    void setPrimaryStage(Stage primaryStage);
}
