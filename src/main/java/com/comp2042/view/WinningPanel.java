package com.comp2042.view;

import com.comp2042.controller.manager.AudioManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Winning panel component displayed when a player wins in multiplayer mode.
 * This JavaFX BorderPane component provides the victory screen interface with
 * a "WINNER!" title, winner player label, victory message, time used display,
 * and two action buttons (RESTART and QUIT). The panel displays which player
 * won the game, shows the time taken to complete the game, and allows players
 * to restart the game or return to the main menu. The panel uses CSS styling
 * classes for theming and supports audio feedback for button interactions
 * (click and hover sounds). The panel is designed to be shown as an overlay
 * when a multiplayer game ends with a winner, providing a clear visual
 * indication of the victory and allowing players to choose their next action.
 */
public class WinningPanel extends BorderPane {

    private Label winnerLabel;
    private Label messageLabel;
    private Label timeUsedLabel;
    private Button restartButton;
    private Button mainMenuButton;
    
    private Runnable onRestartAction;
    private Runnable onMainMenuAction;

    /**
     * Creates a new WinningPanel with all UI components initialized.
     * Sets up the layout with a "WINNER!" title, winner player label (defaults to "PLAYER 1"),
     * message label (defaults to "The other player has lost!"), time used display, and two
     * buttons (RESTART and QUIT) arranged vertically in a centered container. All components
     * are styled with CSS classes. The panel is configured to size based on its content
     * rather than filling available space.
     */
    public WinningPanel() {
        getStyleClass().add("winning-panel");
        
        // Set preferred size constraints
        setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        
        VBox mainContainer = new VBox(30);
        mainContainer.getStyleClass().add("winning-container");
        mainContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // WINNER title
        Label winnerTitle = new Label("WINNER!");
        winnerTitle.getStyleClass().add("winning-title");
        
        // Winner player label
        winnerLabel = new Label("PLAYER 1");
        winnerLabel.getStyleClass().add("winning-player-label");
        
        // Message label
        messageLabel = new Label("The other player has lost!");
        messageLabel.getStyleClass().add("winning-message");
        
        // Time used
        Label timeUsedTitle = new Label("TIME USED:");
        timeUsedTitle.getStyleClass().add("game-over-score-title");
        timeUsedLabel = new Label("00:00");
        timeUsedLabel.getStyleClass().add("game-over-current-score");
        VBox timeBox = new VBox(5);
        timeBox.setAlignment(javafx.geometry.Pos.CENTER);
        timeBox.getChildren().addAll(timeUsedTitle, timeUsedLabel);
        
        // Restart button
        restartButton = new Button("RESTART");
        restartButton.getStyleClass().add("pause-button");
        restartButton.setOnAction(e -> {
            if (onRestartAction != null) {
                onRestartAction.run();
            }
        });
        
        // Back to main menu button
        mainMenuButton = new Button("QUIT");
        mainMenuButton.getStyleClass().add("pause-button");
        mainMenuButton.setOnAction(e -> {
            if (onMainMenuAction != null) {
                onMainMenuAction.run();
            }
        });
        
        mainContainer.getChildren().addAll(winnerTitle, winnerLabel, messageLabel, timeBox, restartButton, mainMenuButton);
        
        setCenter(mainContainer);
    }
    
    /**
     * Sets the winner player number to display.
     * Updates the winner label to show "PLAYER {number}".
     * 
     * @param winnerPlayerNumber The player number (1 or 2) who won the game
     */
    public void setWinner(int winnerPlayerNumber) {
        if (winnerLabel != null) {
            winnerLabel.setText("PLAYER " + winnerPlayerNumber);
        }
    }
    
    /**
     * Sets the victory message to display.
     * 
     * @param message The message string to display (e.g., "The other player has lost!")
     */
    public void setMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }
    
    /**
     * Sets the time used to display on the winning panel.
     * Formats the time in MM:SS format (e.g., "05:23" for 5 minutes 23 seconds).
     * 
     * @param seconds The total time used in seconds
     */
    public void setTimeUsed(int seconds) {
        if (timeUsedLabel != null) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            timeUsedLabel.setText(String.format("%02d:%02d", minutes, secs));
        }
    }
    
    /**
     * Sets the action to execute when the RESTART button is clicked.
     * The RESTART button typically starts a new multiplayer game.
     * 
     * @param action The Runnable to execute when RESTART is clicked, or null to remove the action
     */
    public void setOnRestartAction(Runnable action) {
        this.onRestartAction = action;
    }
    
    /**
     * Sets the action to execute when the QUIT button is clicked.
     * The QUIT button typically returns to the main menu.
     * 
     * @param action The Runnable to execute when QUIT is clicked, or null to remove the action
     */
    public void setOnMainMenuAction(Runnable action) {
        this.onMainMenuAction = action;
    }
    
    /**
     * Sets up button sounds for all buttons in this panel.
     * Configures click and hover sound effects for the RESTART and QUIT buttons
     * using the provided AudioManager. If the audio manager is null, no sounds
     * are configured.
     * 
     * @param audioManager The AudioManager instance to use for playing button sounds
     */
    public void setupButtonSounds(AudioManager audioManager) {
        if (audioManager == null) return;
        
        setupButtonWithSound(restartButton, audioManager);
        setupButtonWithSound(mainMenuButton, audioManager);
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
        
        javafx.event.EventHandler<javafx.event.ActionEvent> originalHandler = button.getOnAction();
        
        button.setOnAction(e -> {
            audioManager.playClickButton();
            if (originalHandler != null) {
                originalHandler.handle(e);
            }
        });
        
        button.setOnMouseEntered(e -> {
            audioManager.playHover();
        });
    }
}

