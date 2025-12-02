package com.comp2042.view;

import com.comp2042.controller.manager.AudioManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Pause panel component displayed when the game is paused.
 * This JavaFX BorderPane component provides the pause menu interface with buttons
 * for resuming the game, restarting the current game, accessing settings, and
 * quitting to the main menu. The panel displays a "PAUSED" title and four action
 * buttons arranged vertically. Each button supports custom action callbacks that
 * are configured externally. The panel uses CSS styling classes for theming and
 * supports audio feedback for button interactions (click and hover sounds). The
 * panel is designed to be shown as an overlay when the game is paused, providing
 * a clear visual indication of the paused state and allowing the player to choose
 * their next action.
 */
public class PausePanel extends BorderPane {

    private Button resumeButton;
    private Button restartButton;
    private Button settingsButton;
    private Button quitButton;
    
    private Runnable onResumeAction;
    private Runnable onRestartAction;
    private Runnable onSettingsAction;
    private Runnable onQuitAction;

    /**
     * Creates a new PausePanel with all UI components initialized.
     * Sets up the layout with a "PAUSED" title and four buttons (RESUME, RESTART,
     * SETTINGS, QUIT) arranged vertically in a centered container. All components
     * are styled with CSS classes. The panel is configured to size based on its
     * content rather than filling available space.
     */
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
    
    /**
     * Sets the action to execute when the RESUME button is clicked.
     * The RESUME button typically unpauses the game.
     * 
     * @param action The Runnable to execute when RESUME is clicked, or null to remove the action
     */
    public void setOnResumeAction(Runnable action) {
        this.onResumeAction = action;
    }
    
    /**
     * Sets the action to execute when the RESTART button is clicked.
     * The RESTART button typically starts a new game.
     * 
     * @param action The Runnable to execute when RESTART is clicked, or null to remove the action
     */
    public void setOnRestartAction(Runnable action) {
        this.onRestartAction = action;
    }
    
    /**
     * Sets the action to execute when the SETTINGS button is clicked.
     * The SETTINGS button typically opens the settings panel.
     * 
     * @param action The Runnable to execute when SETTINGS is clicked, or null to remove the action
     */
    public void setOnSettingsAction(Runnable action) {
        this.onSettingsAction = action;
    }
    
    /**
     * Sets the action to execute when the QUIT button is clicked.
     * The QUIT button typically returns to the main menu.
     * 
     * @param action The Runnable to execute when QUIT is clicked, or null to remove the action
     */
    public void setOnQuitAction(Runnable action) {
        this.onQuitAction = action;
    }
    
    /**
     * Sets up button sounds for all buttons in this panel.
     * Configures click and hover sound effects for the RESUME, RESTART, SETTINGS,
     * and QUIT buttons using the provided AudioManager. If the audio manager is null,
     * no sounds are configured.
     * 
     * @param audioManager The AudioManager instance to use for playing button sounds
     */
    public void setupButtonSounds(AudioManager audioManager) {
        if (audioManager == null) return;
        
        // Add click and hover sounds to all buttons
        setupButtonWithSound(resumeButton, audioManager);
        setupButtonWithSound(restartButton, audioManager);
        setupButtonWithSound(settingsButton, audioManager);
        setupButtonWithSound(quitButton, audioManager);
    }
    
    /**
     * Sets up sound effects for a single button.
     * Wraps the button's existing action handler to play a click sound before
     * executing the original action, and adds a hover sound on mouse enter.
     * Preserves the original action handler if one exists.
     * 
     * @param button The button to configure with sound effects
     * @param audioManager The AudioManager instance to use for playing sounds
     */
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

