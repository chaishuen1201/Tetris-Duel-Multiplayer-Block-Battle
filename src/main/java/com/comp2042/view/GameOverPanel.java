package com.comp2042.view;

import com.comp2042.controller.manager.AudioManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Game over panel component displayed when the game ends.
 * This JavaFX BorderPane component displays the game over screen with the final
 * game statistics including the player's score, time used, and top 3 high scores
 * with medal emojis (🥇🥈🥉). The panel provides two action buttons (YES and NO)
 * for starting a new game or returning to the main menu. The panel uses CSS styling
 * classes for theming and supports audio feedback for button interactions. The
 * panel is designed to be shown over the game board when a game ends, providing
 * a clear visual indication of the game over state and allowing the player to
 * choose their next action.
 */
public class GameOverPanel extends BorderPane {

    private Label currentScoreLabel;
    private Label timeUsedLabel;
    private Label highScore1Label;
    private Label highScore2Label;
    private Label highScore3Label;
    private Button yesButton;
    private Button noButton;
    
    private Runnable onYesAction;
    private Runnable onNoAction;

    /**
     * Creates a new GameOverPanel with all UI components initialized.
     * Sets up the layout with a "GAME OVER" title, "TRY AGAIN?" subtitle,
     * YES and NO buttons, current score display, time used display, and
     * high scores section. All components are styled with CSS classes and
     * arranged in a centered vertical layout.
     */
    public GameOverPanel() {
        getStyleClass().add("game-over-panel");
        
        VBox mainContainer = new VBox(20);
        mainContainer.getStyleClass().add("game-over-container");
        mainContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // GAME OVER title
        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.getStyleClass().add("game-over-title");
        
        // TRY AGAIN? subtitle
        Label tryAgainLabel = new Label("TRY AGAIN?");
        tryAgainLabel.getStyleClass().add("game-over-subtitle");
        
        // YES and NO buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        yesButton = new Button("YES");
        yesButton.getStyleClass().add("game-over-button");
        yesButton.setOnAction(e -> {
            if (onYesAction != null) {
                onYesAction.run();
            }
        });
        
        noButton = new Button("NO");
        noButton.getStyleClass().add("game-over-button");
        noButton.setOnAction(e -> {
            if (onNoAction != null) {
                onNoAction.run();
            }
        });
        
        buttonBox.getChildren().addAll(yesButton, noButton);
        
        // Current score
        Label currentScoreTitle = new Label("YOUR SCORE:");
        currentScoreTitle.getStyleClass().add("game-over-score-title");
        currentScoreLabel = new Label("0");
        currentScoreLabel.getStyleClass().add("game-over-current-score");
        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(javafx.geometry.Pos.CENTER);
        scoreBox.getChildren().addAll(currentScoreTitle, currentScoreLabel);
        
        // Time used
        Label timeUsedTitle = new Label("TIME USED:");
        timeUsedTitle.getStyleClass().add("game-over-score-title");
        timeUsedLabel = new Label("00:00");
        timeUsedLabel.getStyleClass().add("game-over-current-score");
        VBox timeBox = new VBox(5);
        timeBox.setAlignment(javafx.geometry.Pos.CENTER);
        timeBox.getChildren().addAll(timeUsedTitle, timeUsedLabel);
        
        // High scores section
        Label highScoreTitle = new Label("HIGH SCORES:");
        highScoreTitle.getStyleClass().add("game-over-high-score-title");
        
        highScore1Label = new Label("🥇 ---");
        highScore1Label.getStyleClass().addAll("game-over-high-score", "game-over-high-score-gold");
        highScore2Label = new Label("🥈 ---");
        highScore2Label.getStyleClass().addAll("game-over-high-score", "game-over-high-score-silver");
        highScore3Label = new Label("🥉 ---");
        highScore3Label.getStyleClass().addAll("game-over-high-score", "game-over-high-score-bronze");
        
        VBox highScoreBox = new VBox(5);
        highScoreBox.setAlignment(javafx.geometry.Pos.CENTER);
        highScoreBox.getChildren().addAll(highScoreTitle, highScore1Label, highScore2Label, highScore3Label);
        
        // Add all elements to main container
        mainContainer.getChildren().addAll(
            gameOverLabel,
            tryAgainLabel,
            buttonBox,
            scoreBox,
            timeBox,
            highScoreBox
        );
        
        setCenter(mainContainer);
    }
    
    /**
     * Sets the current score to display on the game over panel.
     * 
     * @param score The final score achieved in the game
     */
    public void setCurrentScore(int score) {
        if (currentScoreLabel != null) {
            currentScoreLabel.setText(String.valueOf(score));
        }
    }
    
    /**
     * Sets the time used to display on the game over panel.
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
     * Sets the high scores to display on the game over panel.
     * Displays up to the top 3 high scores with medal emojis (🥇 for 1st, 🥈 for 2nd, 🥉 for 3rd).
     * If the list is null, empty, or has fewer than 3 scores, missing positions are
     * displayed as "---". Only the first 3 scores from the list are displayed.
     * 
     * @param scores A list of high scores, ordered from highest to lowest (at least the top 3)
     */
    public void setHighScores(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            if (highScore1Label != null) highScore1Label.setText("🥇 ---");
            if (highScore2Label != null) highScore2Label.setText("🥈 ---");
            if (highScore3Label != null) highScore3Label.setText("🥉 ---");
            return;
        }
        
        if (highScore1Label != null) {
            highScore1Label.setText(scores.size() > 0 ? "🥇 " + scores.get(0) : "🥇 ---");
        }
        if (highScore2Label != null) {
            highScore2Label.setText(scores.size() > 1 ? "🥈 " + scores.get(1) : "🥈 ---");
        }
        if (highScore3Label != null) {
            highScore3Label.setText(scores.size() > 2 ? "🥉 " + scores.get(2) : "🥉 ---");
        }
    }
    
    /**
     * Sets the action to execute when the YES button is clicked.
     * The YES button typically starts a new game.
     * 
     * @param action The Runnable to execute when YES is clicked, or null to remove the action
     */
    public void setOnYesAction(Runnable action) {
        this.onYesAction = action;
    }
    
    /**
     * Sets the action to execute when the NO button is clicked.
     * The NO button typically returns to the main menu.
     * 
     * @param action The Runnable to execute when NO is clicked, or null to remove the action
     */
    public void setOnNoAction(Runnable action) {
        this.onNoAction = action;
    }
    
    /**
     * Sets up button sounds for all buttons in this panel.
     * Configures click and hover sound effects for the YES and NO buttons
     * using the provided AudioManager. If the audio manager is null, no sounds
     * are configured.
     * 
     * @param audioManager The AudioManager instance to use for playing button sounds
     */
    public void setupButtonSounds(AudioManager audioManager) {
        if (audioManager == null) return;
        
        setupButtonWithSound(yesButton, audioManager);
        setupButtonWithSound(noButton, audioManager);
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

