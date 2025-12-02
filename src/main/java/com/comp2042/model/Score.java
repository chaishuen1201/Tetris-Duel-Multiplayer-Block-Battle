package com.comp2042.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents the game score with JavaFX property binding support.
 * Provides methods to add points, reset the score, and access the score property
 * for binding to UI components.
 */
public final class Score {

    /**
     * Default constructor. Initializes the score to zero.
     */
    public Score() {
        // Score is initialized to 0 by default via SimpleIntegerProperty(0)
    }

    private final IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * Gets the score property for JavaFX binding.
     * 
     * @return The IntegerProperty representing the current score
     */
    public IntegerProperty scoreProperty() {
        return score;
    }

    /**
     * Adds points to the current score.
     * 
     * @param points The number of points to add
     */
    public void add(int points) {
        score.setValue(score.getValue() + points);
    }

    /**
     * Resets the score to zero.
     */
    public void reset() {
        score.setValue(0);
    }
}

