package com.comp2042.controller.manager;

import com.comp2042.view.GameOverPanel;
import com.comp2042.view.PausePanel;
import com.comp2042.view.MainMenuPanel;
import com.comp2042.view.SettingsPanel;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Coordinates visibility and management of all UI panels in the game.
 * Centralizes all setVisible() and setManaged() calls for better organization.
 */
public class PanelCoordinator {
    
    // Panel references
    private BorderPane gameBoard;
    private GridPane gamePanel;
    private GridPane brickPanel;
    private GridPane ghostPanel;
    private GameOverPanel gameOverPanel;
    private PausePanel pausePanel;
    private SettingsPanel settingsPanel;
    private VBox nextBricksPanel;
    private GridPane holdBrickPanel;
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;
    private MainMenuPanel mainMenuPanel;
    private Label countdownLabel;
    
    // Original panels for restoration
    private VBox originalLeftPanel;
    private VBox originalRightPanel;
    
    public PanelCoordinator() {
    }
    
    // Setters for panel references
    public void setGameBoard(BorderPane gameBoard) {
        this.gameBoard = gameBoard;
    }
    
    public void setGamePanel(GridPane gamePanel) {
        this.gamePanel = gamePanel;
    }
    
    public void setBrickPanel(GridPane brickPanel) {
        this.brickPanel = brickPanel;
    }
    
    public void setGhostPanel(GridPane ghostPanel) {
        this.ghostPanel = ghostPanel;
    }
    
    public void setGameOverPanel(GameOverPanel gameOverPanel) {
        this.gameOverPanel = gameOverPanel;
    }
    
    public void setPausePanel(PausePanel pausePanel) {
        this.pausePanel = pausePanel;
    }
    
    public void setSettingsPanel(SettingsPanel settingsPanel) {
        this.settingsPanel = settingsPanel;
    }
    
    public void setNextBricksPanel(VBox nextBricksPanel) {
        this.nextBricksPanel = nextBricksPanel;
    }
    
    public void setHoldBrickPanel(GridPane holdBrickPanel) {
        this.holdBrickPanel = holdBrickPanel;
    }
    
    public void setScoreLabel(Label scoreLabel) {
        this.scoreLabel = scoreLabel;
    }
    
    public void setLevelLabel(Label levelLabel) {
        this.levelLabel = levelLabel;
    }
    
    public void setLinesLabel(Label linesLabel) {
        this.linesLabel = linesLabel;
    }
    
    public void setMainMenuPanel(MainMenuPanel mainMenuPanel) {
        this.mainMenuPanel = mainMenuPanel;
    }
    
    public void setCountdownLabel(Label countdownLabel) {
        this.countdownLabel = countdownLabel;
    }
    
    public void setOriginalLeftPanel(VBox originalLeftPanel) {
        this.originalLeftPanel = originalLeftPanel;
    }
    
    public void setOriginalRightPanel(VBox originalRightPanel) {
        this.originalRightPanel = originalRightPanel;
    }
    
    // Pause Panel visibility
    public void showPausePanel() {
        if (pausePanel != null) {
            pausePanel.setVisible(true);
        }
    }
    
    public void hidePausePanel() {
        if (pausePanel != null) {
            pausePanel.setVisible(false);
        }
    }
    
    // Settings Panel visibility
    public void showSettingsPanel() {
        if (settingsPanel != null) {
            settingsPanel.setVisible(true);
        }
    }
    
    public void hideSettingsPanel() {
        if (settingsPanel != null) {
            settingsPanel.setVisible(false);
        }
    }
    
    public void showSettingsPanelManaged() {
        if (settingsPanel != null) {
            settingsPanel.setManaged(true);
            settingsPanel.setVisible(true);
        }
    }
    
    // Game Over Panel visibility
    public void showGameOverPanel() {
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(true);
        }
    }
    
    public void hideGameOverPanel() {
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
        }
    }
    
    // Main Menu Panel visibility
    public void showMainMenuPanel() {
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(true);
        }
    }
    
    public void hideMainMenuPanel() {
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(false);
        }
    }
    
    // Game Board visibility
    public void showGameBoard() {
        if (gameBoard != null) {
            gameBoard.setVisible(true);
            gameBoard.setManaged(true);
        }
    }
    
    public void hideGameBoard() {
        if (gameBoard != null) {
            gameBoard.setVisible(false);
            gameBoard.setManaged(false);
        }
    }
    
    // Game Panel visibility
    public void showGamePanel() {
        if (gamePanel != null) {
            gamePanel.setVisible(true);
        }
    }
    
    public void hideGamePanel() {
        if (gamePanel != null) {
            gamePanel.setVisible(false);
        }
    }
    
    // Brick Panel visibility
    public void showBrickPanel() {
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
    }
    
    public void hideBrickPanel() {
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }
    }
    
    // Ghost Panel visibility
    public void showGhostPanel(boolean show) {
        if (ghostPanel != null) {
            ghostPanel.setVisible(show);
        }
    }
    
    public void hideGhostPanel() {
        if (ghostPanel != null) {
            ghostPanel.setVisible(false);
        }
    }
    
    // Next Bricks Panel visibility
    public void showNextBricksPanel() {
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(true);
        }
    }
    
    public void hideNextBricksPanel() {
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(false);
        }
    }
    
    public void showNextBricksPanelManaged() {
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(true);
            nextBricksPanel.setManaged(true);
        }
    }
    
    // Hold Brick Panel visibility
    public void showHoldBrickPanel() {
        if (holdBrickPanel != null) {
            holdBrickPanel.setVisible(true);
        }
    }
    
    public void hideHoldBrickPanel() {
        if (holdBrickPanel != null) {
            holdBrickPanel.setVisible(false);
        }
    }
    
    public void showHoldBrickPanelManaged() {
        if (holdBrickPanel != null) {
            holdBrickPanel.setVisible(true);
            holdBrickPanel.setManaged(true);
        }
    }
    
    // Label visibility
    public void showScoreLabel() {
        if (scoreLabel != null) {
            scoreLabel.setVisible(true);
            scoreLabel.setManaged(true);
        }
    }
    
    public void hideScoreLabel() {
        if (scoreLabel != null) {
            scoreLabel.setVisible(false);
        }
    }
    
    public void showLevelLabel() {
        if (levelLabel != null) {
            levelLabel.setVisible(true);
            levelLabel.setManaged(true);
        }
    }
    
    public void hideLevelLabel() {
        if (levelLabel != null) {
            levelLabel.setVisible(false);
        }
    }
    
    public void showLinesLabel() {
        if (linesLabel != null) {
            linesLabel.setVisible(true);
            linesLabel.setManaged(true);
        }
    }
    
    public void hideLinesLabel() {
        if (linesLabel != null) {
            linesLabel.setVisible(false);
        }
    }
    
    // Countdown Label visibility
    public void showCountdownLabel() {
        if (countdownLabel != null) {
            countdownLabel.setVisible(true);
        }
    }
    
    public void hideCountdownLabel() {
        if (countdownLabel != null) {
            countdownLabel.setVisible(false);
        }
    }
    
    // Next Brick Panes visibility (individual panes within nextBricksPanel)
    public void showNextBrickPanes(List<GridPane> nextBrickPanes) {
        if (nextBrickPanes != null) {
            for (GridPane pane : nextBrickPanes) {
                if (pane != null) {
                    pane.setVisible(true);
                }
            }
        }
    }
    
    public void hideNextBrickPanes(List<GridPane> nextBrickPanes) {
        if (nextBrickPanes != null) {
            for (GridPane pane : nextBrickPanes) {
                if (pane != null) {
                    pane.setVisible(false);
                }
            }
        }
    }
    
    public void hideNextBrickPanesManaged(List<GridPane> nextBrickPanes) {
        if (nextBrickPanes != null) {
            for (GridPane pane : nextBrickPanes) {
                if (pane != null) {
                    pane.setVisible(false);
                    pane.setManaged(true); // Keep managed so layout works, just hide visually
                }
            }
        }
    }
    
    // Original Panels (left/right VBoxes) visibility
    public void showOriginalLeftPanel(BorderPane rootPane) {
        if (originalLeftPanel != null && rootPane != null) {
            originalLeftPanel.setVisible(true);
            originalLeftPanel.setManaged(true);
            rootPane.setLeft(originalLeftPanel);
            originalLeftPanel.requestLayout();
        }
    }
    
    public void showOriginalRightPanel(BorderPane rootPane) {
        if (originalRightPanel != null && rootPane != null) {
            originalRightPanel.setVisible(true);
            originalRightPanel.setManaged(true);
            rootPane.setRight(originalRightPanel);
            originalRightPanel.requestLayout();
        }
    }
    
    public void hideOriginalLeftPanel(BorderPane rootPane) {
        if (rootPane != null && rootPane.getLeft() != null) {
            rootPane.getLeft().setVisible(false);
            rootPane.getLeft().setManaged(false);
        }
    }
    
    public void hideOriginalRightPanel(BorderPane rootPane) {
        if (rootPane != null && rootPane.getRight() != null) {
            rootPane.getRight().setVisible(false);
            rootPane.getRight().setManaged(false);
        }
    }
    
    // Show all children of a VBox panel
    public void showAllChildrenOfPanel(VBox panel) {
        if (panel != null) {
            for (javafx.scene.Node child : panel.getChildren()) {
                child.setVisible(true);
                child.setManaged(true);
            }
        }
    }
    
    // Hide all single player panels for multiplayer mode
    public void hideSinglePlayerPanels() {
        hideHoldBrickPanel();
        hideNextBricksPanel();
        hideScoreLabel();
        hideLevelLabel();
        hideLinesLabel();
    }
    
    // Show all single player panels
    public void showSinglePlayerPanels() {
        showHoldBrickPanelManaged();
        showNextBricksPanelManaged();
        showScoreLabel();
        showLevelLabel();
        showLinesLabel();
    }
    
    // Initialize panel states (called during initialization)
    public void initializePanelStates() {
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
        }
        if (pausePanel != null) {
            pausePanel.setVisible(false);
        }
        if (countdownLabel != null) {
            countdownLabel.setVisible(false);
        }
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }
        if (ghostPanel != null) {
            ghostPanel.setVisible(false);
        }
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(true);
        }
        if (settingsPanel != null) {
            settingsPanel.setVisible(false);
        }
    }
}

