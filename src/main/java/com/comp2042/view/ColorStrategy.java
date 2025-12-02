package com.comp2042.view;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Utility class providing color mapping for different brick types.
 * Maps brick type integers to JavaFX Paint colors for rendering.
 * Includes special handling for garbage blocks (type 8) and transparent cells (type 0).
 * This is a utility class and cannot be instantiated.
 */
public final class ColorStrategy {

    private ColorStrategy() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Gets the color for a specific brick type.
     * 
     * @param brickType The brick type integer (0-8, where 0 is transparent, 8 is garbage)
     * @return The Paint color corresponding to the brick type, or white as default
     */
    public static Paint getColorForBrickType(int brickType) {
        return switch (brickType) {
            case 0 -> Color.TRANSPARENT;
            case 1 -> Color.AQUA;
            case 2 -> Color.BLUEVIOLET;
            case 3 -> Color.DARKGREEN;
            case 4 -> Color.YELLOW;
            case 5 -> Color.RED;
            case 6 -> Color.BEIGE;
            case 7 -> Color.BURLYWOOD;
            case 8 -> Color.DARKGRAY; // Garbage blocks (darker gray for better visibility)
            default -> Color.WHITE;
        };
    }
}

