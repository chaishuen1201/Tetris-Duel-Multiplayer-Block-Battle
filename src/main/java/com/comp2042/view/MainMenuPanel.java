package com.comp2042.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MainMenuPanel extends BorderPane {

    final Button playButton;
    final Button quitButton;

    public MainMenuPanel() {
        VBox menuBox = new VBox(30);
        menuBox.getStyleClass().add("main-menu-container");

        // Title
        Label titleLabel = new Label("TETRIS");
        titleLabel.getStyleClass().add("main-menu-title");
        
        // Load and apply PublicPixel font
        try {
            Font publicPixelFont = Font.loadFont(getClass().getClassLoader().getResource("PublicPixel-rv0pA.ttf").toExternalForm(), 48);
            if (publicPixelFont != null) {
                titleLabel.setFont(publicPixelFont);
                // Also set the font family name for CSS compatibility
                String fontFamily = publicPixelFont.getFamily();
                titleLabel.setStyle("-fx-font-family: '" + fontFamily + "';");
            }
        } catch (Exception e) {
            System.out.println("PublicPixel font not found, using default font: " + e.getMessage());
        }

        // Buttons container
        VBox buttonBox = new VBox(15);
        buttonBox.getStyleClass().add("main-menu-button-group");

        playButton = new Button("PLAY");
        playButton.getStyleClass().add("menu-button");

        quitButton = new Button("QUIT");
        quitButton.getStyleClass().add("menu-button");

        buttonBox.getChildren().addAll(playButton, quitButton);
        menuBox.getChildren().addAll(titleLabel, buttonBox);
        setCenter(menuBox);
    }

    public Button getPlayButton() {
        return playButton;
    }

    public Button getQuitButton() {
        return quitButton;
    }
}
