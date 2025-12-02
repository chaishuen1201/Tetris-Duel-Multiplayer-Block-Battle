package com.comp2042;

import com.comp2042.controller.GameController;
import com.comp2042.controller.GuiController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the Tetris game application.
 * Initializes the JavaFX application, loads the FXML layout, and sets up the game controller.
 */
public class Main extends Application {

    /**
     * Default constructor. Initializes the Main application class.
     */
    public Main() {
        // Default constructor - initialization handled by start() method
    }

    private static final String FXML_RESOURCE = "/gameLayout.fxml";
    private static final String WINDOW_TITLE = "TetrisJFX";

    /**
     * Initializes and displays the main application window.
     * Loads the FXML layout file, sets up the GUI controller, and initializes the game controller.
     * 
     * @param primaryStage The primary stage for the JavaFX application
     * @throws Exception If there is an error loading the FXML file or initializing the application
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML_RESOURCE));
        Parent root = fxmlLoader.load();
        GuiController guiController = fxmlLoader.getController();

        primaryStage.setTitle(WINDOW_TITLE);
        Scene scene = new Scene(root, 1230, 770);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initialize game controller with view
        new GameController(guiController);
    }

    /**
     * Main method to launch the JavaFX application.
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
