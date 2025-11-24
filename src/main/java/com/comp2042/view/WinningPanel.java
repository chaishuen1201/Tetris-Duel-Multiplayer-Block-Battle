package com.comp2042.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class WinningPanel extends BorderPane {

    private Label winnerLabel;
    private Label messageLabel;
    private Button restartButton;
    private Button mainMenuButton;
    
    private Runnable onRestartAction;
    private Runnable onMainMenuAction;

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
        
        mainContainer.getChildren().addAll(winnerTitle, winnerLabel, messageLabel, restartButton, mainMenuButton);
        
        setCenter(mainContainer);
    }
    
    public void setWinner(int winnerPlayerNumber) {
        if (winnerLabel != null) {
            winnerLabel.setText("PLAYER " + winnerPlayerNumber);
        }
    }
    
    public void setMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }
    
    public void setOnRestartAction(Runnable action) {
        this.onRestartAction = action;
    }
    
    public void setOnMainMenuAction(Runnable action) {
        this.onMainMenuAction = action;
    }
}

