package com.comp2042.view;

import com.comp2042.controller.GameConstants;
import com.comp2042.controller.GameController;
import com.comp2042.event.InputEventListener;
import com.comp2042.logic.bricks.Brick;
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
}

