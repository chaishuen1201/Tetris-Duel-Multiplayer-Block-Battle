package com.comp2042.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class PausePanel extends BorderPane {

    private Button resumeButton;
    private Button restartButton;
    private Button quitButton;
    
    private Runnable onResumeAction;
    private Runnable onRestartAction;
    private Runnable onQuitAction;

    public PausePanel() {
        getStyleClass().add("pause-panel");
        
        // Set preferred size constraints to ensure consistent sizing
        // The panel should size based on its content, not fill available space
        setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        
        VBox mainContainer = new VBox(20);
        mainContainer.getStyleClass().add("pause-container");
        mainContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // PAUSE title
        Label pauseLabel = new Label("PAUSED");
        pauseLabel.getStyleClass().add("pause-title");
        
        // Resume button
        resumeButton = new Button("RESUME");
        resumeButton.getStyleClass().add("pause-button");
        resumeButton.setOnAction(e -> {
            if (onResumeAction != null) {
                onResumeAction.run();
            }
        });
        
        // Restart button
        restartButton = new Button("RESTART");
        restartButton.getStyleClass().add("pause-button");
        restartButton.setOnAction(e -> {
            if (onRestartAction != null) {
                onRestartAction.run();
            }
        });
        
        // Quit to main menu button
        quitButton = new Button("QUIT");
        quitButton.getStyleClass().add("pause-button");
        quitButton.setOnAction(e -> {
            if (onQuitAction != null) {
                onQuitAction.run();
            }
        });
        
        // Add all elements to main container
        mainContainer.getChildren().addAll(
            pauseLabel,
            resumeButton,
            restartButton,
            quitButton
        );
        
        setCenter(mainContainer);
    }
    
    public void setOnResumeAction(Runnable action) {
        this.onResumeAction = action;
    }
    
    public void setOnRestartAction(Runnable action) {
        this.onRestartAction = action;
    }
    
    public void setOnQuitAction(Runnable action) {
        this.onQuitAction = action;
    }
}

