package com.comp2042.controller;

/**
 * Centralized game constants to avoid magic numbers throughout the codebase.
 * This class provides a single location for all game-related constants including
 * board dimensions (width, height, brick size, grid gap), calculated panel dimensions,
 * game timing values (soft drop rate), and player number constants. By centralizing
 * these values, the codebase follows the DRY (Don't Repeat Yourself) principle,
 * improves maintainability, and makes it easier to adjust game parameters. All
 * constants are public static final, and the class cannot be instantiated.
 */
public final class GameConstants {
    
    // Board dimensions
    /** Size of each brick cell in pixels */
    public static final int BRICK_SIZE = 30;
    /** Gap between brick cells in pixels */
    public static final int GRID_GAP = 1;
    /** Width of the game board in cells */
    public static final int BOARD_WIDTH = 10;
    /** Height of the game board in cells */
    public static final int BOARD_HEIGHT = 20;
    
    // Calculated panel dimensions
    /** Calculated width of the game panel in pixels */
    public static final int GAME_PANEL_WIDTH = (BRICK_SIZE * BOARD_WIDTH) + (GRID_GAP * (BOARD_WIDTH - 1));
    /** Calculated height of the game panel in pixels */
    public static final int GAME_PANEL_HEIGHT = (BRICK_SIZE * BOARD_HEIGHT) + (GRID_GAP * (BOARD_HEIGHT - 1));
    
    // Game timing
    /** Rate multiplier for soft drop (faster than normal drop) */
    public static final double SOFT_DROP_RATE = 12.0;
    
    // Player numbers
    /** Player number constant for single player mode */
    public static final int SINGLE_PLAYER = 0;
    /** Player number constant for player 1 in multiplayer mode */
    public static final int PLAYER_1 = 1;
    /** Player number constant for player 2 in multiplayer mode */
    public static final int PLAYER_2 = 2;
    
    /**
     * Private constructor to prevent instantiation of this utility class.
     * Throws an AssertionError if called, as this class is meant to be used
     * statically only.
     * 
     * @throws AssertionError Always thrown if an attempt is made to instantiate this class
     */
    private GameConstants() {
        throw new AssertionError("Cannot instantiate GameConstants");
    }
}

