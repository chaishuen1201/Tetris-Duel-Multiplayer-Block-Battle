package com.comp2042.view;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class WinningPanel extends BorderPane {

    private Label winnerLabel;
    private Label messageLabel;

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
        
        mainContainer.getChildren().addAll(winnerTitle, winnerLabel, messageLabel);
        
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
}

