package com.comp2042.view;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public final class ColorStrategy {

    private ColorStrategy() {
        throw new AssertionError("Utility class should not be instantiated");
    }

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
            default -> Color.WHITE;
        };
    }
}

