package com.comp2042.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.util.List;

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
    
    public void setCurrentScore(int score) {
        if (currentScoreLabel != null) {
            currentScoreLabel.setText(String.valueOf(score));
        }
    }
    
    public void setTimeUsed(int seconds) {
        if (timeUsedLabel != null) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            timeUsedLabel.setText(String.format("%02d:%02d", minutes, secs));
        }
    }
    
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
    
    public void setOnYesAction(Runnable action) {
        this.onYesAction = action;
    }
    
    public void setOnNoAction(Runnable action) {
        this.onNoAction = action;
    }
}

