package com.comp2042.controller;

/**
 * Centralized game constants to avoid magic numbers throughout the codebase.
 * Follows DRY principle and improves maintainability.
 */
public final class GameConstants {
    
    // Board dimensions
    public static final int BRICK_SIZE = 30;
    public static final int GRID_GAP = 1;
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;
    
    // Calculated panel dimensions
    public static final int GAME_PANEL_WIDTH = (BRICK_SIZE * BOARD_WIDTH) + (GRID_GAP * (BOARD_WIDTH - 1));
    public static final int GAME_PANEL_HEIGHT = (BRICK_SIZE * BOARD_HEIGHT) + (GRID_GAP * (BOARD_HEIGHT - 1));
    
    // Game timing
    public static final double SOFT_DROP_RATE = 12.0;
    
    // Player numbers
    public static final int SINGLE_PLAYER = 0;
    public static final int PLAYER_1 = 1;
    public static final int PLAYER_2 = 2;
    
    // Prevent instantiation
    private GameConstants() {
        throw new AssertionError("Cannot instantiate GameConstants");
    }
}

