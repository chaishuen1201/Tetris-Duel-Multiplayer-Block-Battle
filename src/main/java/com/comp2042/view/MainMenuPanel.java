package com.comp2042.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * Main menu panel component displaying the game's main menu.
 * This JavaFX BorderPane component serves as the entry point to the game,
 * providing navigation buttons for starting a single player game (PLAY),
 * starting a multiplayer game (MULTIPLAYER), accessing settings (SETTINGS),
 * and quitting the application (QUIT). The panel features a "TETRIS" title
 * using a custom PublicPixel font for a retro game aesthetic, and all
 * buttons are styled with CSS classes. The panel uses a vertical layout
 * with the title at the top and buttons arranged below it. Button actions
 * are configured externally by accessing the button references through getter methods.
 */
public class MainMenuPanel extends BorderPane {

    final Button playButton;
    final Button settingsButton;
    final Button multiButton;
    final Button quitButton;

    /**
     * Creates a new MainMenuPanel with all UI components initialized.
     * Sets up the layout with a "TETRIS" title using the PublicPixel font,
     * and four menu buttons (PLAY, MULTIPLAYER, SETTINGS, QUIT) arranged
     * vertically. All components are styled with CSS classes. If the PublicPixel
     * font cannot be loaded, the title falls back to the default font.
     */
    public MainMenuPanel() {
        VBox menuBox = new VBox(30);
        menuBox.getStyleClass().add("main-menu-container");

        // Title
        Label titleLabel = new Label("TETRIS");
        titleLabel.getStyleClass().add("main-menu-title");
        
        // Load and apply PublicPixel font
        try {
            java.net.URL fontUrl = getClass().getClassLoader().getResource("font/PublicPixel-rv0pA.ttf");
            if (fontUrl != null) {
                Font publicPixelFont = Font.loadFont(fontUrl.toExternalForm(), 48);
                if (publicPixelFont != null) {
                    titleLabel.setFont(publicPixelFont);
                    // Also set the font family name for CSS compatibility
                    String fontFamily = publicPixelFont.getFamily();
                    titleLabel.setStyle("-fx-font-family: '" + fontFamily + "';");
                }
            } else {
                System.out.println("PublicPixel font not found: font/PublicPixel-rv0pA.ttf");
            }
        } catch (Exception e) {
            System.out.println("PublicPixel font not found, using default font: " + e.getMessage());
        }

        // Buttons container
        VBox buttonBox = new VBox(15);
        buttonBox.getStyleClass().add("main-menu-button-group");

        playButton = new Button("PLAY");
        playButton.getStyleClass().add("menu-button");

        multiButton = new Button("MULTIPLAYER");
        multiButton.getStyleClass().add("menu-button");

        settingsButton = new Button("SETTINGS");
        settingsButton.getStyleClass().add("menu-button");

        quitButton = new Button("QUIT");
        quitButton.getStyleClass().add("menu-button");

        buttonBox.getChildren().addAll(playButton, multiButton, settingsButton, quitButton);
        menuBox.getChildren().addAll(titleLabel, buttonBox);
        setCenter(menuBox);
    }

    /**
     * Gets the PLAY button for starting a single player game.
     * 
     * @return The Button instance for the PLAY action
     */
    public Button getPlayButton() {
        return playButton;
    }

    /**
     * Gets the SETTINGS button for accessing game settings.
     * 
     * @return The Button instance for the SETTINGS action
     */
    public Button getSettingsButton() {
        return settingsButton;
    }

    /**
     * Gets the QUIT button for exiting the application.
     * 
     * @return The Button instance for the QUIT action
     */
    public Button getQuitButton() {
        return quitButton;
    }

    /**
     * Gets the MULTIPLAYER button for starting a multiplayer game.
     * 
     * @return The Button instance for the MULTIPLAYER action
     */
    public Button getMultiButton() {
        return multiButton;
    }
}
