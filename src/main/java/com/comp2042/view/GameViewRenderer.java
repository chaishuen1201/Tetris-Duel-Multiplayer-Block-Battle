package com.comp2042.view;

import com.comp2042.controller.GameConstants;
import com.comp2042.controller.GameController;
import com.comp2042.event.InputEventListener;
import com.comp2042.bricks.Brick;
import com.comp2042.model.SimpleBoard;
import com.comp2042.model.ViewData;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

/**
 * Centralized rendering class for all game view rendering operations.
 * Handles drawing of bricks, ghost pieces, board background, next bricks, and hold bricks.
 */
public class GameViewRenderer {

    /**
     * Default constructor. Initializes the GameViewRenderer.
     */
    public GameViewRenderer() {
        // Default constructor - all methods are static utility methods
    }
    
    private static final int BOARD_WIDTH = GameConstants.BOARD_WIDTH;
    private static final int BOARD_HEIGHT = GameConstants.BOARD_HEIGHT;
    
    /**
     * Renders the current brick and ghost piece on the game board.
     * 
     * @param brick The brick data to render
     * @param brickPanel The GridPane to draw the brick on
     * @param ghostPanel The GridPane to draw the ghost piece on (can be null)
     * @param eventListener The event listener to get board state for ghost position
     * @param scaledBrickSize The scaled brick size (for multiplayer scaling)
     */
    public void refreshBrick(ViewData brick, GridPane brickPanel, GridPane ghostPanel, 
                            InputEventListener eventListener, int scaledBrickSize) {
        if (brickPanel == null || brick == null) {
            return;
        }
        
        // Clear both panels
        brickPanel.getChildren().clear();
        if (ghostPanel != null) {
            ghostPanel.getChildren().clear();
        }
        
        int[][] data = brick.getBrickData();
        int offsetX = brick.getXPosition();
        int offsetY = brick.getYPosition();
        
        // Draw ghost piece first (behind the actual brick)
        if (ghostPanel != null && eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            if (gameController.getBoard() instanceof SimpleBoard) {
                SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                java.awt.Point ghostPos = simpleBoard.getGhostPosition();
                
                int ghostX = (int) ghostPos.getX();
                int ghostY = (int) ghostPos.getY();
                
                // Only show ghost if it's below the current position
                if (ghostY > offsetY) {
                    drawGhostPiece(data, ghostPanel, ghostX, ghostY, scaledBrickSize);
                }
            }
        }
        
        // Draw actual brick on top
        drawBrick(data, brickPanel, offsetX, offsetY, scaledBrickSize);
    }
    
    /**
     * Draws the ghost piece on the ghost panel.
     */
    private void drawGhostPiece(int[][] data, GridPane ghostPanel, int ghostX, int ghostY, int scaledBrickSize) {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (data[j][i] != 0) {
                    int cellX = ghostX + i;
                    int cellY = ghostY + j;
                    
                    // Only draw if the cell is within the board bounds
                    // This allows partial ghost display when brick is at left/right walls
                    if (cellX >= 0 && cellY >= 0 && cellX < BOARD_WIDTH && cellY < BOARD_HEIGHT) {
                        Rectangle ghostRect = new Rectangle(scaledBrickSize, scaledBrickSize);
                        
                        // Get the brick's color and make it semi-transparent
                        javafx.scene.paint.Paint brickColor = ColorStrategy.getColorForBrickType(data[j][i]);
                        if (brickColor instanceof javafx.scene.paint.Color) {
                            javafx.scene.paint.Color color = (javafx.scene.paint.Color) brickColor;
                            // Create a semi-transparent version (30% opacity)
                            javafx.scene.paint.Color ghostColor = new javafx.scene.paint.Color(
                                color.getRed(),
                                color.getGreen(),
                                color.getBlue(),
                                0.3
                            );
                            ghostRect.setFill(ghostColor);
                            // Use a slightly darker stroke for visibility
                            javafx.scene.paint.Color strokeColor = new javafx.scene.paint.Color(
                                color.getRed() * 0.7,
                                color.getGreen() * 0.7,
                                color.getBlue() * 0.7,
                                0.5
                            );
                            ghostRect.setStroke(strokeColor);
                            ghostRect.setStrokeWidth(1.5);
                        } else {
                            // Fallback if color is not a Color object
                            ghostRect.setFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.3));
                            ghostRect.setStroke(javafx.scene.paint.Color.rgb(200, 200, 200, 0.5));
                            ghostRect.setStrokeWidth(1.5);
                        }
                        
                        ghostRect.setArcHeight(5);
                        ghostRect.setArcWidth(5);
                        ghostPanel.add(ghostRect, cellX, cellY);
                    }
                }
            }
        }
    }
    
    /**
     * Draws the actual brick on the brick panel.
     */
    private void drawBrick(int[][] data, GridPane brickPanel, int offsetX, int offsetY, int scaledBrickSize) {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (data[j][i] != 0) {
                    Rectangle rect = new Rectangle(scaledBrickSize, scaledBrickSize);
                    rect.setFill(ColorStrategy.getColorForBrickType(data[j][i]));
                    rect.setArcHeight(5);
                    rect.setArcWidth(5);
                    brickPanel.add(rect, offsetX + i, offsetY + j);
                }
            }
        }
    }
    
    /**
     * Refreshes the game board background by updating the display matrix colors.
     * 
     * @param board The board matrix to render
     * @param displayMatrix The Rectangle matrix representing the board cells
     */
    public void refreshGameBackground(int[][] board, Rectangle[][] displayMatrix) {
        if (displayMatrix == null) {
            return;
        }
        
        for (int i = 0; i < Math.min(BOARD_HEIGHT, board.length); i++) {
            for (int j = 0; j < Math.min(BOARD_WIDTH, board[i].length); j++) {
                if (displayMatrix[i][j] != null) {
                    displayMatrix[i][j].setFill(ColorStrategy.getColorForBrickType(board[i][j]));
                }
            }
        }
    }
    
    /**
     * Updates the next bricks preview display.
     * 
     * @param nextBricks The list of next bricks to display
     * @param nextBrickPanes The list of GridPanes to render bricks in
     * @param brickSize The size of each brick cell
     * @param maxBricks The maximum number of bricks to display
     */
    public void updateNextBricks(List<Brick> nextBricks, List<GridPane> nextBrickPanes, 
                                int brickSize, int maxBricks) {
        if (nextBrickPanes == null || nextBrickPanes.isEmpty()) {
            return;
        }
        
        for (int i = 0; i < Math.min(maxBricks, nextBrickPanes.size()); i++) {
            GridPane pane = nextBrickPanes.get(i);
            if (pane == null) {
                continue;
            }
            pane.getChildren().clear();
            
            if (i < nextBricks.size()) {
                int[][] shape = nextBricks.get(i).getShapeMatrix().get(0);
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] != 0) {
                            Rectangle rect = new Rectangle(brickSize, brickSize);
                            rect.setFill(ColorStrategy.getColorForBrickType(shape[r][c]));
                            rect.setArcHeight(5);
                            rect.setArcWidth(5);
                            pane.add(rect, c, r);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Renders the held brick in the hold panel.
     * 
     * @param heldBrick The brick to render (can be null to clear)
     * @param holdBrickRectangles The Rectangle matrix for the hold panel
     */
    public void renderHoldBrick(Brick heldBrick, Rectangle[][] holdBrickRectangles) {
        if (holdBrickRectangles == null) {
            return;
        }
        
        // Clear all rectangles
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (holdBrickRectangles[i][j] != null) {
                    holdBrickRectangles[i][j].setFill(Color.TRANSPARENT);
                }
            }
        }
        
        // Draw the held brick if it exists
        if (heldBrick != null) {
            int[][] shape = heldBrick.getShapeMatrix().get(0);
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] != 0 && holdBrickRectangles[i][j] != null) {
                        holdBrickRectangles[i][j].setFill(ColorStrategy.getColorForBrickType(shape[i][j]));
                    }
                }
            }
        }
    }
    
    /**
     * Clears the display matrix by setting all cells to transparent.
     * 
     * @param displayMatrix The Rectangle matrix to clear
     */
    public void clearDisplayMatrix(Rectangle[][] displayMatrix) {
        if (displayMatrix == null) {
            return;
        }
        
        for (int i = 0; i < displayMatrix.length; i++) {
            for (int j = 0; j < displayMatrix[i].length; j++) {
                if (displayMatrix[i][j] != null) {
                    displayMatrix[i][j].setFill(Color.TRANSPARENT);
                }
            }
        }
    }
    
    /**
     * Clears and re-initializes next brick panes with transparent rectangles.
     * 
     * @param nextBrickPanes The list of GridPanes to clear and re-initialize
     * @param brickSize The size of each brick cell
     */
    public void clearNextBrickPanes(List<GridPane> nextBrickPanes, int brickSize) {
        if (nextBrickPanes == null) {
            return;
        }
        
        for (GridPane pane : nextBrickPanes) {
            if (pane != null) {
                pane.getChildren().clear();
                // Re-initialize empty cells
                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < 4; c++) {
                        Rectangle rect = new Rectangle(brickSize, brickSize);
                        rect.setFill(Color.TRANSPARENT);
                        pane.add(rect, c, r);
                    }
                }
            }
        }
    }
    
    // ========== MultiplayerScreen-specific rendering methods ==========
    
    /**
     * Refreshes the brick display for a multiplayer player.
     * 
     * @param screen The MultiplayerScreen instance
     * @param brick The brick data to render
     * @param playerNumber The player number (1 or 2)
     */
    public void refreshBrick(MultiplayerScreen screen, ViewData brick, int playerNumber) {
        if (screen == null || brick == null) {
            return;
        }
        
        // Store brick data in screen
        screen.setCurrentBrickData(brick, playerNumber);
        
        GridPane brickPanel = screen.getBrickPanel(playerNumber);
        GridPane ghostPanel = screen.getGhostPanel(playerNumber);
        InputEventListener eventListener = screen.getEventListener(playerNumber);
        
        double scale = 0.85;
        int scaledBrickSize = (int)(GameConstants.BRICK_SIZE * scale);
        
        refreshBrick(brick, brickPanel, ghostPanel, eventListener, scaledBrickSize);
    }
    
    /**
     * Refreshes the game background for a multiplayer player.
     * 
     * @param screen The MultiplayerScreen instance
     * @param board The board matrix to render
     * @param playerNumber The player number (1 or 2)
     */
    public void refreshGameBackground(MultiplayerScreen screen, int[][] board, int playerNumber) {
        if (screen == null) {
            return;
        }
        
        Rectangle[][] matrix = screen.getDisplayMatrix(playerNumber);
        refreshGameBackground(board, matrix);
    }
    
    /**
     * Updates the next bricks display for a multiplayer player.
     * 
     * @param screen The MultiplayerScreen instance
     * @param nextBricks The list of next bricks to display
     * @param playerNumber The player number (1 or 2)
     */
    public void updateNextBricks(MultiplayerScreen screen, List<Brick> nextBricks, int playerNumber) {
        if (screen == null) {
            return;
        }
        
        List<GridPane> panes = screen.getNextBrickPanes(playerNumber);
        double scale = 0.85;
        int brickSize = (int)((GameConstants.BRICK_SIZE - 10) * scale);
        
        if (panes == null || panes.isEmpty()) {
            return;
        }
        
        // For multiplayer, only show the first brick
        int maxBricks = 1;
        
        updateNextBricks(nextBricks, panes, brickSize, maxBricks);
    }
    
    /**
     * Updates the hold brick display for a multiplayer player.
     * 
     * @param screen The MultiplayerScreen instance
     * @param heldBrick The brick to render (can be null to clear)
     * @param playerNumber The player number (1 or 2)
     */
    public void updateHoldBrick(MultiplayerScreen screen, Brick heldBrick, int playerNumber) {
        if (screen == null) {
            return;
        }
        
        Rectangle[][] rectangles = screen.getHoldBrickRectangles(playerNumber);
        renderHoldBrick(heldBrick, rectangles);
    }
    
    /**
     * Clears all brick panels for multiplayer (brick panels, ghost panels, hold panels, next panels).
     * 
     * @param screen The MultiplayerScreen instance
     */
    public void clearBrickPanels(MultiplayerScreen screen) {
        if (screen == null) {
            return;
        }
        
        // Clear display matrices
        clearDisplayMatrix(screen.getDisplayMatrix(1));
        clearDisplayMatrix(screen.getDisplayMatrix(2));
        
        // Clear brick panels
        double scale = 0.85;
        int brickSize = (int)(GameConstants.BRICK_SIZE * scale);
        
        GridPane brickPanel1 = screen.getBrickPanel(1);
        GridPane brickPanel2 = screen.getBrickPanel(2);
        GridPane ghostPanel1 = screen.getGhostPanel(1);
        GridPane ghostPanel2 = screen.getGhostPanel(2);
        
        if (brickPanel1 != null) {
            brickPanel1.getChildren().clear();
            initializeBrickPanel(brickPanel1, brickSize);
        }
        if (brickPanel2 != null) {
            brickPanel2.getChildren().clear();
            initializeBrickPanel(brickPanel2, brickSize);
        }
        
        // Clear ghost panels
        if (ghostPanel1 != null) {
            ghostPanel1.getChildren().clear();
            initializeBrickPanel(ghostPanel1, brickSize);
        }
        if (ghostPanel2 != null) {
            ghostPanel2.getChildren().clear();
            initializeBrickPanel(ghostPanel2, brickSize);
        }
        
        // Clear hold panels
        int holdBrickSize = (int)((GameConstants.BRICK_SIZE - 10) * scale);
        GridPane holdPanel1 = screen.getHoldBrickPanel(1);
        GridPane holdPanel2 = screen.getHoldBrickPanel(2);
        Rectangle[][] holdRectangles1 = screen.getHoldBrickRectangles(1);
        Rectangle[][] holdRectangles2 = screen.getHoldBrickRectangles(2);
        
        if (holdPanel1 != null) {
            holdPanel1.getChildren().clear();
            if (holdRectangles1 == null) {
                holdRectangles1 = new Rectangle[4][4];
                screen.setHoldBrickRectangles(holdRectangles1, 1);
            }
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Rectangle rect = new Rectangle(holdBrickSize, holdBrickSize);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.gray(0.3));
                    holdRectangles1[i][j] = rect;
                    holdPanel1.add(rect, j, i);
                }
            }
        }
        
        if (holdPanel2 != null) {
            holdPanel2.getChildren().clear();
            if (holdRectangles2 == null) {
                holdRectangles2 = new Rectangle[4][4];
                screen.setHoldBrickRectangles(holdRectangles2, 2);
            }
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Rectangle rect = new Rectangle(holdBrickSize, holdBrickSize);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.gray(0.3));
                    holdRectangles2[i][j] = rect;
                    holdPanel2.add(rect, j, i);
                }
            }
        }
        
        // Clear next bricks panels
        int nextBrickSize = (int)(80 * scale);
        List<GridPane> nextPanes1 = screen.getNextBrickPanes(1);
        List<GridPane> nextPanes2 = screen.getNextBrickPanes(2);
        javafx.scene.layout.VBox nextBricksPanel1 = screen.getNextBricksPanel(1);
        javafx.scene.layout.VBox nextBricksPanel2 = screen.getNextBricksPanel(2);
        
        // Clear and re-initialize next brick panes
        if (nextBricksPanel1 != null) {
            nextBricksPanel1.getChildren().clear();
            if (nextPanes1 == null) {
                nextPanes1 = new java.util.ArrayList<>();
                screen.setNextBrickPanes(nextPanes1, 1);
            }
            nextPanes1.clear();
            GridPane pane = new GridPane();
            pane.setVgap(1);
            pane.setHgap(1);
            pane.setPrefSize(nextBrickSize, nextBrickSize);
            // Set max size to prevent expansion when wide pieces (like I-piece) are displayed
            pane.setMaxSize(nextBrickSize, nextBrickSize);
            pane.setMinSize(nextBrickSize, nextBrickSize);
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    Rectangle rect = new Rectangle(holdBrickSize, holdBrickSize);
                    rect.setFill(Color.TRANSPARENT);
                    pane.add(rect, c, r);
                }
            }
            nextPanes1.add(pane);
            nextBricksPanel1.getChildren().add(pane);
        }
        
        if (nextBricksPanel2 != null) {
            nextBricksPanel2.getChildren().clear();
            if (nextPanes2 == null) {
                nextPanes2 = new java.util.ArrayList<>();
                screen.setNextBrickPanes(nextPanes2, 2);
            }
            nextPanes2.clear();
            GridPane pane = new GridPane();
            pane.setVgap(1);
            pane.setHgap(1);
            pane.setPrefSize(nextBrickSize, nextBrickSize);
            // Set max size to prevent expansion when wide pieces (like I-piece) are displayed
            pane.setMaxSize(nextBrickSize, nextBrickSize);
            pane.setMinSize(nextBrickSize, nextBrickSize);
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    Rectangle rect = new Rectangle(holdBrickSize, holdBrickSize);
                    rect.setFill(Color.TRANSPARENT);
                    pane.add(rect, c, r);
                }
            }
            nextPanes2.add(pane);
            nextBricksPanel2.getChildren().add(pane);
        }
    }
    
    /**
     * Helper method to initialize a brick panel with empty cells.
     */
    private void initializeBrickPanel(GridPane panel, int brickSize) {
        if (panel == null) {
            return;
        }
        
        panel.getColumnConstraints().clear();
        panel.getRowConstraints().clear();
        
        for (int c = 0; c < BOARD_WIDTH; c++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints(brickSize);
            panel.getColumnConstraints().add(cc);
        }
        
        for (int r = 0; r < BOARD_HEIGHT; r++) {
            javafx.scene.layout.RowConstraints rc = new javafx.scene.layout.RowConstraints(brickSize);
            panel.getRowConstraints().add(rc);
        }
    }
    
}

