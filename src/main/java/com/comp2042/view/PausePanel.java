package com.comp2042.view;

import com.comp2042.controller.manager.AudioManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class PausePanel extends BorderPane {

    private Button resumeButton;
    private Button restartButton;
    private Button settingsButton;
    private Button quitButton;
    
    private Runnable onResumeAction;
    private Runnable onRestartAction;
    private Runnable onSettingsAction;
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
        
        // Settings button
        settingsButton = new Button("SETTINGS");
        settingsButton.getStyleClass().add("pause-button");
        settingsButton.setOnAction(e -> {
            if (onSettingsAction != null) {
                onSettingsAction.run();
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
            settingsButton,
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
    
    public void setOnSettingsAction(Runnable action) {
        this.onSettingsAction = action;
    }
    
    public void setOnQuitAction(Runnable action) {
        this.onQuitAction = action;
    }
    
    /**
     * Sets up button sounds for all buttons in this panel.
     * @param audioManager The audio manager to use for playing sounds
     */
    public void setupButtonSounds(AudioManager audioManager) {
        if (audioManager == null) return;
        
        // Add click and hover sounds to all buttons
        setupButtonWithSound(resumeButton, audioManager);
        setupButtonWithSound(restartButton, audioManager);
        setupButtonWithSound(settingsButton, audioManager);
        setupButtonWithSound(quitButton, audioManager);
    }
    
    private void setupButtonWithSound(Button button, AudioManager audioManager) {
        if (button == null || audioManager == null) return;
        
        // Store original action
        javafx.event.EventHandler<javafx.event.ActionEvent> originalHandler = button.getOnAction();
        
        // Wrap with click sound
        button.setOnAction(e -> {
            audioManager.playClickButton();
            if (originalHandler != null) {
                originalHandler.handle(e);
            }
        });
        
        // Add hover sound
        button.setOnMouseEntered(e -> {
            audioManager.playHover();
        });
    }
}

