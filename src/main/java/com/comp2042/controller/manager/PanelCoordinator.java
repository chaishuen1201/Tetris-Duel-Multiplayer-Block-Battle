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
 * This class centralizes all setVisible() and setManaged() calls for better
 * organization and maintainability. It provides a single point of control for
 * showing and hiding various game panels including the game board, brick panels,
 * ghost panels, pause panel, game over panel, main menu panel, settings panel,
 * next bricks panel, hold brick panel, labels (score, level, lines), countdown
 * label, and original left/right panels. The coordinator supports both single
 * player and multiplayer modes, handles panel state initialization, and manages
 * transitions between different game states. It follows the Single Responsibility
 * Principle by exclusively handling UI panel visibility management.
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
    
    // New centered layout container
    private HBox gameContainerWrapper;
    private javafx.scene.Parent gameContainerParent; // Can be StackPane or VBox
    
    /**
     * Creates a new PanelCoordinator instance.
     * All panel references must be set using the setter methods before use.
     */
    public PanelCoordinator() {
    }
    
    // Setters for panel references
    /**
     * Sets the game board BorderPane reference.
     * 
     * @param gameBoard The BorderPane containing the game board
     */
    public void setGameBoard(BorderPane gameBoard) {
        this.gameBoard = gameBoard;
    }
    
    /**
     * Sets the game panel GridPane reference.
     * 
     * @param gamePanel The GridPane for the game grid display
     */
    public void setGamePanel(GridPane gamePanel) {
        this.gamePanel = gamePanel;
    }
    
    /**
     * Sets the brick panel GridPane reference.
     * 
     * @param brickPanel The GridPane for displaying the current brick
     */
    public void setBrickPanel(GridPane brickPanel) {
        this.brickPanel = brickPanel;
    }
    
    /**
     * Sets the ghost panel GridPane reference.
     * 
     * @param ghostPanel The GridPane for displaying the ghost piece preview
     */
    public void setGhostPanel(GridPane ghostPanel) {
        this.ghostPanel = ghostPanel;
    }
    
    /**
     * Sets the game over panel reference.
     * 
     * @param gameOverPanel The GameOverPanel instance
     */
    public void setGameOverPanel(GameOverPanel gameOverPanel) {
        this.gameOverPanel = gameOverPanel;
    }
    
    /**
     * Sets the pause panel reference.
     * 
     * @param pausePanel The PausePanel instance
     */
    public void setPausePanel(PausePanel pausePanel) {
        this.pausePanel = pausePanel;
    }
    
    /**
     * Sets the settings panel reference.
     * 
     * @param settingsPanel The SettingsPanel instance
     */
    public void setSettingsPanel(SettingsPanel settingsPanel) {
        this.settingsPanel = settingsPanel;
    }
    
    /**
     * Sets the next bricks panel VBox reference.
     * 
     * @param nextBricksPanel The VBox container for next brick previews
     */
    public void setNextBricksPanel(VBox nextBricksPanel) {
        this.nextBricksPanel = nextBricksPanel;
    }
    
    /**
     * Sets the hold brick panel GridPane reference.
     * 
     * @param holdBrickPanel The GridPane for displaying the held brick
     */
    public void setHoldBrickPanel(GridPane holdBrickPanel) {
        this.holdBrickPanel = holdBrickPanel;
    }
    
    /**
     * Sets the score label reference.
     * 
     * @param scoreLabel The Label for displaying score
     */
    public void setScoreLabel(Label scoreLabel) {
        this.scoreLabel = scoreLabel;
    }
    
    /**
     * Sets the level label reference.
     * 
     * @param levelLabel The Label for displaying level
     */
    public void setLevelLabel(Label levelLabel) {
        this.levelLabel = levelLabel;
    }
    
    /**
     * Sets the lines label reference.
     * 
     * @param linesLabel The Label for displaying lines cleared
     */
    public void setLinesLabel(Label linesLabel) {
        this.linesLabel = linesLabel;
    }
    
    /**
     * Sets the main menu panel reference.
     * 
     * @param mainMenuPanel The MainMenuPanel instance
     */
    public void setMainMenuPanel(MainMenuPanel mainMenuPanel) {
        this.mainMenuPanel = mainMenuPanel;
    }
    
    /**
     * Sets the countdown label reference.
     * 
     * @param countdownLabel The Label for displaying countdown numbers
     */
    public void setCountdownLabel(Label countdownLabel) {
        this.countdownLabel = countdownLabel;
    }
    
    /**
     * Sets the original left panel VBox reference for restoration.
     * 
     * @param originalLeftPanel The original left panel VBox
     */
    public void setOriginalLeftPanel(VBox originalLeftPanel) {
        this.originalLeftPanel = originalLeftPanel;
    }
    
    /**
     * Sets the original right panel VBox reference for restoration.
     * 
     * @param originalRightPanel The original right panel VBox
     */
    public void setOriginalRightPanel(VBox originalRightPanel) {
        this.originalRightPanel = originalRightPanel;
    }
    
    /**
     * Sets the game container wrapper HBox reference for centered layout.
     * 
     * @param gameContainerWrapper The HBox wrapper for the game container
     */
    public void setGameContainerWrapper(HBox gameContainerWrapper) {
        this.gameContainerWrapper = gameContainerWrapper;
    }
    
    /**
     * Sets the game container parent reference (can be StackPane or VBox).
     * 
     * @param gameContainerParent The Parent node containing the game container
     */
    public void setGameContainerParent(javafx.scene.Parent gameContainerParent) {
        this.gameContainerParent = gameContainerParent;
    }
    
    /**
     * Legacy method for backward compatibility.
     * Sets the game container parent as a StackPane.
     * 
     * @param gameContainerStackPane The StackPane containing the game container
     */
    public void setGameContainerStackPane(StackPane gameContainerStackPane) {
        this.gameContainerParent = gameContainerStackPane;
    }
    
    // Pause Panel visibility
    /**
     * Shows the pause panel.
     */
    public void showPausePanel() {
        if (pausePanel != null) {
            pausePanel.setVisible(true);
        }
    }
    
    /**
     * Hides the pause panel.
     */
    public void hidePausePanel() {
        if (pausePanel != null) {
            pausePanel.setVisible(false);
        }
    }
    
    // Settings Panel visibility
    /**
     * Shows the settings panel.
     */
    public void showSettingsPanel() {
        if (settingsPanel != null) {
            settingsPanel.setVisible(true);
        }
    }
    
    /**
     * Hides the settings panel.
     */
    public void hideSettingsPanel() {
        if (settingsPanel != null) {
            settingsPanel.setVisible(false);
        }
    }
    
    /**
     * Shows the settings panel and sets it as managed for layout purposes.
     */
    public void showSettingsPanelManaged() {
        if (settingsPanel != null) {
            settingsPanel.setManaged(true);
            settingsPanel.setVisible(true);
        }
    }
    
    // Game Over Panel visibility
    /**
     * Shows the game over panel.
     */
    public void showGameOverPanel() {
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(true);
        }
    }
    
    /**
     * Hides the game over panel.
     */
    public void hideGameOverPanel() {
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
        }
    }
    
    // Main Menu Panel visibility
    /**
     * Shows the main menu panel.
     */
    public void showMainMenuPanel() {
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(true);
        }
    }
    
    /**
     * Hides the main menu panel.
     */
    public void hideMainMenuPanel() {
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(false);
        }
    }
    
    // Game Board visibility
    /**
     * Shows the game board and sets it as managed for layout purposes.
     */
    public void showGameBoard() {
        if (gameBoard != null) {
            gameBoard.setVisible(true);
            gameBoard.setManaged(true);
        }
    }
    
    /**
     * Hides the game board and sets it as unmanaged.
     */
    public void hideGameBoard() {
        if (gameBoard != null) {
            gameBoard.setVisible(false);
            gameBoard.setManaged(false);
        }
    }
    
    // Game Panel visibility
    /**
     * Shows the game panel (game grid display).
     */
    public void showGamePanel() {
        if (gamePanel != null) {
            gamePanel.setVisible(true);
        }
    }
    
    /**
     * Hides the game panel (game grid display).
     */
    public void hideGamePanel() {
        if (gamePanel != null) {
            gamePanel.setVisible(false);
        }
    }
    
    // Brick Panel visibility
    /**
     * Shows the brick panel (current brick display).
     */
    public void showBrickPanel() {
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
    }
    
    /**
     * Hides the brick panel (current brick display).
     */
    public void hideBrickPanel() {
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }
    }
    
    // Ghost Panel visibility
    /**
     * Shows or hides the ghost panel based on the parameter.
     * 
     * @param show True to show the ghost panel, false to hide it
     */
    public void showGhostPanel(boolean show) {
        if (ghostPanel != null) {
            ghostPanel.setVisible(show);
        }
    }
    
    /**
     * Hides the ghost panel.
     */
    public void hideGhostPanel() {
        if (ghostPanel != null) {
            ghostPanel.setVisible(false);
        }
    }
    
    // Next Bricks Panel visibility
    /**
     * Shows the next bricks panel.
     */
    public void showNextBricksPanel() {
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(true);
        }
    }
    
    /**
     * Hides the next bricks panel.
     */
    public void hideNextBricksPanel() {
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(false);
        }
    }
    
    /**
     * Shows the next bricks panel and sets it as managed for layout purposes.
     */
    public void showNextBricksPanelManaged() {
        if (nextBricksPanel != null) {
            nextBricksPanel.setVisible(true);
            nextBricksPanel.setManaged(true);
        }
    }
    
    // Hold Brick Panel visibility
    /**
     * Shows the hold brick panel.
     */
    public void showHoldBrickPanel() {
        if (holdBrickPanel != null) {
            holdBrickPanel.setVisible(true);
        }
    }
    
    /**
     * Hides the hold brick panel.
     */
    public void hideHoldBrickPanel() {
        if (holdBrickPanel != null) {
            holdBrickPanel.setVisible(false);
        }
    }
    
    /**
     * Shows the hold brick panel and sets it as managed for layout purposes.
     */
    public void showHoldBrickPanelManaged() {
        if (holdBrickPanel != null) {
            holdBrickPanel.setVisible(true);
            holdBrickPanel.setManaged(true);
        }
    }
    
    // Label visibility
    /**
     * Shows the score label and sets it as managed for layout purposes.
     */
    public void showScoreLabel() {
        if (scoreLabel != null) {
            scoreLabel.setVisible(true);
            scoreLabel.setManaged(true);
        }
    }
    
    /**
     * Hides the score label.
     */
    public void hideScoreLabel() {
        if (scoreLabel != null) {
            scoreLabel.setVisible(false);
        }
    }
    
    /**
     * Shows the level label and sets it as managed for layout purposes.
     */
    public void showLevelLabel() {
        if (levelLabel != null) {
            levelLabel.setVisible(true);
            levelLabel.setManaged(true);
        }
    }
    
    /**
     * Hides the level label.
     */
    public void hideLevelLabel() {
        if (levelLabel != null) {
            levelLabel.setVisible(false);
        }
    }
    
    /**
     * Shows the lines label and sets it as managed for layout purposes.
     */
    public void showLinesLabel() {
        if (linesLabel != null) {
            linesLabel.setVisible(true);
            linesLabel.setManaged(true);
        }
    }
    
    /**
     * Hides the lines label.
     */
    public void hideLinesLabel() {
        if (linesLabel != null) {
            linesLabel.setVisible(false);
        }
    }
    
    // Countdown Label visibility
    /**
     * Shows the countdown label.
     */
    public void showCountdownLabel() {
        if (countdownLabel != null) {
            countdownLabel.setVisible(true);
        }
    }
    
    /**
     * Hides the countdown label.
     */
    public void hideCountdownLabel() {
        if (countdownLabel != null) {
            countdownLabel.setVisible(false);
        }
    }
    
    // Next Brick Panes visibility (individual panes within nextBricksPanel)
    /**
     * Shows all next brick panes in the provided list.
     * 
     * @param nextBrickPanes The list of GridPane instances for next brick previews
     */
    public void showNextBrickPanes(List<GridPane> nextBrickPanes) {
        if (nextBrickPanes != null) {
            for (GridPane pane : nextBrickPanes) {
                if (pane != null) {
                    pane.setVisible(true);
                }
            }
        }
    }
    
    /**
     * Hides all next brick panes in the provided list.
     * 
     * @param nextBrickPanes The list of GridPane instances for next brick previews
     */
    public void hideNextBrickPanes(List<GridPane> nextBrickPanes) {
        if (nextBrickPanes != null) {
            for (GridPane pane : nextBrickPanes) {
                if (pane != null) {
                    pane.setVisible(false);
                }
            }
        }
    }
    
    /**
     * Hides all next brick panes but keeps them managed for layout purposes.
     * This ensures the layout still works correctly even when the panes are hidden.
     * 
     * @param nextBrickPanes The list of GridPane instances for next brick previews
     */
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
    /**
     * Shows the original left panel, supporting both new centered layout and old layout.
     * For the new centered layout, shows the HBox wrapper. For the old layout,
     * shows the original left panel and sets it in the root pane.
     * 
     * @param rootPane The root BorderPane containing the panels
     */
    public void showOriginalLeftPanel(BorderPane rootPane) {
        // First try to show the HBox wrapper (new centered layout)
        if (gameContainerWrapper != null) {
            gameContainerWrapper.setVisible(true);
            gameContainerWrapper.setManaged(true);
            gameContainerWrapper.requestLayout();
            return;
        }
        // Fallback to old layout
        if (originalLeftPanel != null && rootPane != null) {
            originalLeftPanel.setVisible(true);
            originalLeftPanel.setManaged(true);
            rootPane.setLeft(originalLeftPanel);
            originalLeftPanel.requestLayout();
        }
    }
    
    /**
     * Shows the original right panel, supporting both new centered layout and old layout.
     * For the new centered layout, this is handled by showOriginalLeftPanel.
     * For the old layout, shows the original right panel and sets it in the root pane.
     * 
     * @param rootPane The root BorderPane containing the panels
     */
    public void showOriginalRightPanel(BorderPane rootPane) {
        // In the new centered layout, showing left panel also shows right panel (they're in the same wrapper)
        if (gameContainerWrapper != null) {
            // Already handled by showOriginalLeftPanel
            return;
        }
        // Fallback to old layout
        if (originalRightPanel != null && rootPane != null) {
            originalRightPanel.setVisible(true);
            originalRightPanel.setManaged(true);
            rootPane.setRight(originalRightPanel);
            originalRightPanel.requestLayout();
        }
    }
    
    /**
     * Hides the original left panel, supporting both new centered layout and old layout.
     * For the new centered layout, hides the HBox wrapper. For the old layout,
     * hides the left panel in the root pane.
     * 
     * @param rootPane The root BorderPane containing the panels
     */
    public void hideOriginalLeftPanel(BorderPane rootPane) {
        // First try to hide the HBox wrapper directly
        if (gameContainerWrapper != null) {
            gameContainerWrapper.setVisible(false);
            gameContainerWrapper.setManaged(false);
            return;
        }
        // Fallback to old layout if wrapper not found
        if (rootPane != null && rootPane.getLeft() != null) {
            rootPane.getLeft().setVisible(false);
            rootPane.getLeft().setManaged(false);
        }
    }
    
    /**
     * Hides the original right panel, supporting both new centered layout and old layout.
     * For the new centered layout, this is handled by hideOriginalLeftPanel.
     * For the old layout, hides the right panel in the root pane.
     * 
     * @param rootPane The root BorderPane containing the panels
     */
    public void hideOriginalRightPanel(BorderPane rootPane) {
        // In the new centered layout, hiding left panel also hides right panel (they're in the same wrapper)
        // So we only need to check if the wrapper isn't already hidden
        if (gameContainerWrapper != null) {
            // Already handled by hideOriginalLeftPanel
            return;
        }
        // Fallback to old layout if wrapper not found
        if (rootPane != null && rootPane.getRight() != null) {
            rootPane.getRight().setVisible(false);
            rootPane.getRight().setManaged(false);
        }
    }
    
    /**
     * Shows all children of a VBox panel by setting them visible and managed.
     * 
     * @param panel The VBox panel whose children should be shown
     */
    public void showAllChildrenOfPanel(VBox panel) {
        if (panel != null) {
            for (javafx.scene.Node child : panel.getChildren()) {
                child.setVisible(true);
                child.setManaged(true);
            }
        }
    }
    
    /**
     * Hides all single player panels for multiplayer mode.
     * Hides hold brick panel, next bricks panel, and all labels (score, level, lines).
     */
    public void hideSinglePlayerPanels() {
        hideHoldBrickPanel();
        hideNextBricksPanel();
        hideScoreLabel();
        hideLevelLabel();
        hideLinesLabel();
    }
    
    /**
     * Shows all single player panels.
     * Shows hold brick panel, next bricks panel, and all labels (score, level, lines)
     * with managed state set appropriately.
     */
    public void showSinglePlayerPanels() {
        showHoldBrickPanelManaged();
        showNextBricksPanelManaged();
        showScoreLabel();
        showLevelLabel();
        showLinesLabel();
    }
    
    /**
     * Shows the single player container wrapper for centered layout.
     */
    public void showSinglePlayerContainerWrapper() {
        if (gameContainerWrapper != null) {
            gameContainerWrapper.setVisible(true);
            gameContainerWrapper.setManaged(true);
        }
    }
    
    /**
     * Hides the single player container wrapper for centered layout.
     */
    public void hideSinglePlayerContainerWrapper() {
        if (gameContainerWrapper != null) {
            gameContainerWrapper.setVisible(false);
            gameContainerWrapper.setManaged(false);
        }
    }
    
    /**
     * Initializes panel states to their default visibility during application startup.
     * Hides game over panel, pause panel, countdown label, brick panel, and ghost panel.
     * Shows next bricks panel. Hides settings panel.
     */
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

