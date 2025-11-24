package com.comp2042;

import com.comp2042.controller.GameController;
import com.comp2042.controller.GuiController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String FXML_RESOURCE = "/gameLayout.fxml";
    private static final String WINDOW_TITLE = "TetrisJFX";

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML_RESOURCE));
        Parent root = fxmlLoader.load();
        GuiController guiController = fxmlLoader.getController();

        primaryStage.setTitle(WINDOW_TITLE);
        Scene scene = new Scene(root, 1210, 710);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initialize game controller with view
        new GameController(guiController);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
