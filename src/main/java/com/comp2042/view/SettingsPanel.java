package com.comp2042.view;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

public class SettingsPanel extends BorderPane {

    private Slider volumeSlider;
    private Button muteButton;
    private CheckBox ghostPieceCheckBox;
    private Button backButton;

    private Runnable onBackAction;

    public SettingsPanel() {
        getStyleClass().addAll("settings-panel", "animated");

        VBox mainContainer = new VBox(30);
        mainContainer.getStyleClass().add("settings-container");
        mainContainer.setAlignment(Pos.CENTER);

        // SETTINGS title with enhanced styling
        Label settingsLabel = new Label("SETTINGS");
        settingsLabel.getStyleClass().add("settings-title");

        // Volume control section with glass morphism effect
        VBox volumeBox = new VBox(20);
        volumeBox.getStyleClass().add("volume-section");
        volumeBox.setAlignment(Pos.CENTER);

        Label volumeLabel = new Label("VOLUME");
        volumeLabel.getStyleClass().add("settings-label");

        HBox volumeControlBox = new HBox(20);
        volumeControlBox.getStyleClass().add("volume-control-container");
        volumeControlBox.setAlignment(Pos.CENTER);

        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.getStyleClass().add("volume-slider");
        volumeSlider.setShowTickLabels(false);
        volumeSlider.setShowTickMarks(false);
        volumeSlider.setPrefWidth(250);

        muteButton = new Button("🔊");
        muteButton.getStyleClass().add("settings-button");

        volumeControlBox.getChildren().addAll(volumeSlider, muteButton);
        volumeBox.getChildren().addAll(volumeLabel, volumeControlBox);

        // Ghost piece checkbox section
        VBox ghostPieceBox = new VBox(15);
        ghostPieceBox.setAlignment(Pos.CENTER);

        Label ghostPieceLabel = new Label("GHOST PIECE");
        ghostPieceLabel.getStyleClass().add("settings-label");

        ghostPieceCheckBox = new CheckBox("Show ghost piece");
        ghostPieceCheckBox.getStyleClass().add("settings-checkbox");
        ghostPieceCheckBox.setSelected(true); // Default to checked

        ghostPieceBox.getChildren().addAll(ghostPieceLabel, ghostPieceCheckBox);

        // Back button with consistent main menu styling
        backButton = new Button("BACK");
        backButton.getStyleClass().add("menu-button");
        backButton.setOnAction(e -> {
            if (onBackAction != null) {
                onBackAction.run();
            }
        });

        mainContainer.getChildren().addAll(settingsLabel, volumeBox, ghostPieceBox, backButton);
        setCenter(mainContainer);
    }

    public Slider getVolumeSlider() {
        return volumeSlider;
    }

    public Button getMuteButton() {
        return muteButton;
    }

    public CheckBox getGhostPieceCheckBox() {
        return ghostPieceCheckBox;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setOnBackAction(Runnable action) {
        this.onBackAction = action;
    }
}