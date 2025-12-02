package com.comp2042.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Notification panel component for displaying temporary messages (e.g., score bonuses).
 * Shows animated notifications that fade out and move upward before being removed.
 * Used for displaying score bonuses, line clear notifications, and other game events.
 */
public class NotificationPanel extends BorderPane {

    /**
     * Creates a new notification panel with the specified text.
     * 
     * @param text The text to display in the notification
     */
    public NotificationPanel(String text) {
        setMinHeight(200);
        setMinWidth(220);
        final Label score = new Label(text);
        score.getStyleClass().add("bonusStyle");
        final Effect glow = new Glow(0.6);
        score.setEffect(glow);
        score.setTextFill(Color.WHITE);
        setCenter(score);
    }

    /**
     * Displays the notification with a fade-out and upward movement animation.
     * Automatically removes itself from the parent list after the animation completes.
     * 
     * @param list The ObservableList containing this panel (used for removal after animation)
     */
    public void showScore(ObservableList<Node> list) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(2000), this);
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(2500), this);
        translateTransition.setToY(this.getLayoutY() - 40);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        ParallelTransition transition = new ParallelTransition(translateTransition, fadeTransition);
        transition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                list.remove(NotificationPanel.this);
            }
        });
        transition.play();
    }
}

