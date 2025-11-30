package com.comp2042.controller;

import com.comp2042.controller.manager.AudioManager;
import com.comp2042.controller.manager.GameStateManager;
import com.comp2042.controller.input.InputHandler;
import com.comp2042.event.InputEventListener;
import com.comp2042.model.SimpleBoard;
import com.comp2042.view.MainMenuPanel;
import com.comp2042.view.MultiplayerScreen;
import com.comp2042.view.PausePanel;
import com.comp2042.view.SettingsPanel;
import com.comp2042.view.SinglePlayerScreen;
import com.comp2042.view.GameOverPanel;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

/**
 * Handles all pause panel action setup and execution.
 * Extracted from GuiController to improve separation of concerns.
 */
public class PausePanelActionHandler {
    
    private final GuiController guiController;
    
    public PausePanelActionHandler(GuiController guiController) {
        this.guiController = guiController;
    }
    
    /**
     * Sets up all action handlers for the pause panel.
     * @param panel The pause panel to configure
     */
    public void setupPausePanelActions(PausePanel panel) {
        // Resume action - just resume the game
        panel.setOnResumeAction(() -> {
            guiController.pauseGame(null);
        });
        
        // Settings action - show settings panel
        panel.setOnSettingsAction(() -> {
            guiController.getSettingsController().showSettingsFromPause();
        });
        
        // Restart action - start a new game
        panel.setOnRestartAction(() -> {
            handleRestartAction();
        });
        
        // Quit to main menu action
        panel.setOnQuitAction(() -> {
            handleQuitAction();
        });
    }
    
    /**
     * Sets up all action handlers for the multiplayer pause panel.
     * This method is used by MultiplayerScreen to set up its pause panel actions.
     * @param panel The multiplayer pause panel to configure
     */
    public void setupMultiplayerPausePanelActions(PausePanel panel) {
        // Resume action - just resume the game
        panel.setOnResumeAction(() -> {
            guiController.pauseGame(null);
        });
        
        // Settings action - show settings panel
        panel.setOnSettingsAction(() -> {
            guiController.getSettingsController().showSettingsFromPause();
        });
        
        // Restart action - start a new game
        panel.setOnRestartAction(() -> {
            handleRestartAction();
        });
        
        // Quit to main menu action
        panel.setOnQuitAction(() -> {
            handleQuitAction();
        });
    }
    
    private void handleRestartAction() {
        GameStateManager gameStateManager = guiController.getGameStateManager();
        MultiplayerScreen multiplayerScreen = guiController.getMultiplayerScreen();
        PausePanel pausePanel = guiController.getPausePanel();
        
        if (gameStateManager.isMultiplayerMode()) {
            // Hide pause panel before restarting
            if (pausePanel != null) {
                pausePanel.setVisible(false);
            }
            if (multiplayerScreen != null) {
                multiplayerScreen.hidePausePanel();
            }
            
            // Hide winning panel if visible
            gameStateManager.hideWinningPanel();
            
            // Use MultiplayerViewManager to restart, which properly handles ghost panel visibility
            guiController.getMultiplayerViewManager().restartMultiplayerGame();
        } else {
            // For single player, use newGame method
            guiController.newGame(null);
            if (pausePanel != null) {
                pausePanel.setVisible(false);
            }
        }
    }
    
    private void handleQuitAction() {
        GameStateManager gameStateManager = guiController.getGameStateManager();
        MultiplayerScreen multiplayerScreen = guiController.getMultiplayerScreen();
        PausePanel pausePanel = guiController.getPausePanel();
        SinglePlayerScreen singlePlayerScreen = guiController.getSinglePlayerScreen();
        AudioManager audioManager = guiController.getAudioManager();
        InputHandler inputHandler = guiController.getInputHandler();
        
        // Stop all timelines
        Timeline timeLine = guiController.getTimeLine();
        Timeline timeLine1 = guiController.getTimeLine1();
        Timeline timeLine2 = guiController.getTimeLine2();
        if (timeLine != null) timeLine.stop();
        if (timeLine1 != null) timeLine1.stop();
        if (timeLine2 != null) timeLine2.stop();
        
        // Hide pause panels
        if (pausePanel != null) {
            pausePanel.setVisible(false);
        }
        if (multiplayerScreen != null) {
            multiplayerScreen.hidePausePanel();
        }
        
        // Check multiplayer mode BEFORE calling quitToMainMenu (which sets it to false)
        boolean wasMultiplayer = gameStateManager.isMultiplayerMode();
        
        // If multiplayer, use the same method as winning panel (it handles timeline updates)
        if (wasMultiplayer) {
            guiController.getMultiplayerViewManager().quitToMainMenuFromMultiplayer();
            return;
        }
        
        // For single player, update GameStateManager with current references
        gameStateManager.setTimeLine(timeLine);
        gameStateManager.setTimeLine1(timeLine1);
        gameStateManager.setTimeLine2(timeLine2);
        
        // For single player, delegate to GameStateManager
        gameStateManager.quitToMainMenu();
        
        // Hide winning panel if visible
        gameStateManager.hideWinningPanel();
        
        // Hide game over panel if visible
        GameOverPanel gameOverPanel = guiController.getGameOverPanel();
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
        }
        
        // Stop game music and play main menu music
        audioManager.stopAll();
        audioManager.playMainMenuMusic();
        
        // Handle single player quit
        handleSinglePlayerQuit(singlePlayerScreen);
        
        // Show main menu
        MainMenuPanel mainMenuPanel = guiController.getMainMenuPanel();
        if (mainMenuPanel != null) {
            mainMenuPanel.setVisible(true);
        }
        
        // Ensure gameBoard is visible for single player mode (main menu is inside it)
        BorderPane gameBoard = guiController.getGameBoard();
        if (!gameStateManager.isMultiplayerMode() && gameBoard != null) {
            gameBoard.setVisible(true);
            gameBoard.setManaged(true);
        }
        
        // Hide game board panels
        GridPane brickPanel = guiController.getBrickPanel();
        GridPane ghostPanel = guiController.getGhostPanel();
        GridPane gamePanel = guiController.getGamePanel();
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }
        if (ghostPanel != null) {
            ghostPanel.setVisible(false);
        }
        // Show game panel (background grid) for main menu - it should be visible
        if (!gameStateManager.isMultiplayerMode() && gamePanel != null) {
            gamePanel.setVisible(true);
        }
    }
    
    private void handleSinglePlayerQuit(SinglePlayerScreen singlePlayerScreen) {
        InputEventListener eventListener = guiController.getEventListener();
        GridPane brickPanel = guiController.getBrickPanel();
        GridPane ghostPanel = guiController.getGhostPanel();
        GridPane gamePanel = guiController.getGamePanel();
        
        // Reset the game board state
        if (eventListener != null) {
            eventListener.createNewGame();
        }
        // Clear the game board display
        if (singlePlayerScreen != null) {
            Rectangle[][] displayMatrix = singlePlayerScreen.getDisplayMatrix();
            if (displayMatrix != null) {
                for (int i = 0; i < displayMatrix.length; i++) {
                    for (int j = 0; j < displayMatrix[i].length; j++) {
                        if (displayMatrix[i][j] != null) {
                            displayMatrix[i][j].setFill(Color.TRANSPARENT);
                        }
                    }
                }
            }
            // Clear next bricks display
            List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
            if (nextBrickPanes != null) {
                for (GridPane pane : nextBrickPanes) {
                    if (pane != null) {
                        pane.getChildren().clear();
                        // Re-initialize empty cells
                        for (int r = 0; r < 4; r++) {
                            for (int c = 0; c < 4; c++) {
                                Rectangle rect = new Rectangle(GameConstants.BRICK_SIZE - 10, GameConstants.BRICK_SIZE - 10);
                                rect.setFill(Color.TRANSPARENT);
                                pane.add(rect, c, r);
                            }
                        }
                    }
                }
            }
            // Clear hold brick display
            Rectangle[][] holdBrickRectangles = singlePlayerScreen.getHoldBrickRectangles();
            if (holdBrickRectangles != null) {
                for (int i = 0; i < holdBrickRectangles.length; i++) {
                    for (int j = 0; j < holdBrickRectangles[i].length; j++) {
                        if (holdBrickRectangles[i][j] != null) {
                            holdBrickRectangles[i][j].setFill(Color.TRANSPARENT);
                        }
                    }
                }
            }
        }
        // Clear current brick display and ghost panel
        if (brickPanel != null) {
            brickPanel.getChildren().clear();
            brickPanel.setVisible(false);
        }
        if (ghostPanel != null) {
            ghostPanel.getChildren().clear();
            ghostPanel.setVisible(false);
        }
        // Hide game panel (background grid) but keep gameBoard visible for main menu
        if (gamePanel != null) {
            gamePanel.setVisible(false);
        }
        // Keep gameBoard visible so main menu can be shown
        // The main menu panel is inside gameBoard's StackPane
    }
    
    private void handleMultiplayerQuit(GameStateManager gameStateManager, MultiplayerScreen multiplayerScreen, 
                                      InputHandler inputHandler, AudioManager audioManager) {
        SettingsPanel settingsPanel = guiController.getSettingsPanel();
        StackPane gameStack = guiController.getGameStack();
        BorderPane gameBoard = guiController.getGameBoard();
        GridPane gamePanel = guiController.getGamePanel();
        GridPane holdBrickPanel = guiController.getHoldBrickPanel();
        VBox nextBricksPanel = guiController.getNextBricksPanel();
        SinglePlayerScreen singlePlayerScreen = guiController.getSinglePlayerScreen();
        
        // Clear all multiplayer game panels and bricks before quitting
        guiController.clearMultiplayerGamePanels();
        
        // Clear multiplayer references via GameStateManager
        gameStateManager.clearMultiplayerReferences();
        gameStateManager.setMultiplayerMode(false);
        
        // Clear local references
        guiController.stopGarbageProcessingTimelines();
        guiController.stopMultiplayerTimer();
        guiController.setGameController1(null);
        guiController.setGameController2(null);
        guiController.setEventListener1(null);
        guiController.setEventListener2(null);
        guiController.setTimeLine1(null);
        guiController.setTimeLine2(null);
        guiController.setGarbageProcessTimeline1(null);
        guiController.setGarbageProcessTimeline2(null);
        
        // Hide multiplayer container and wrapper first
        if (multiplayerScreen != null) {
            multiplayerScreen.hide();
            multiplayerScreen.hidePausePanel();
            gameStateManager.hideWinningPanel();
            multiplayerScreen.hideSettingsOverlay();
            // Move settings panel back to gameStack for single player mode
            if (settingsPanel != null) {
                settingsPanel.setVisible(false);
            }
        }
        
        // Remove multiplayer wrapper and container from center VBox
        if (gameBoard != null && gameBoard.getParent() != null) {
            javafx.scene.Parent parent = gameBoard.getParent();
            if (parent instanceof javafx.scene.layout.VBox) {
                javafx.scene.layout.VBox centerVBox = (javafx.scene.layout.VBox) parent;
                if (multiplayerScreen != null) {
                    // Remove wrapper if present
                    javafx.scene.layout.StackPane wrapper = multiplayerScreen.getWrapper();
                    if (wrapper != null && centerVBox.getChildren().contains(wrapper)) {
                        centerVBox.getChildren().remove(wrapper);
                    }
                    // Also check and remove container directly if it was added separately
                    javafx.scene.layout.HBox container = multiplayerScreen.getContainer();
                    if (container != null && centerVBox.getChildren().contains(container)) {
                        centerVBox.getChildren().remove(container);
                    }
                }
                // Ensure gameBoard is visible and request layout update
                if (gameBoard != null) {
                    gameBoard.setVisible(true);
                    gameBoard.setManaged(true);
                }
                centerVBox.requestLayout();
            }
        }
        
        // Hide winning panel if visible (already hidden above, but ensure it's hidden)
        gameStateManager.hideWinningPanel();
        
        // Ensure settings panel is in gameStack for single player mode (do this regardless of overlay state)
        if (settingsPanel != null && gameStack != null) {
            // Remove from any other parent first (defensive)
            javafx.scene.Parent currentParent = settingsPanel.getParent();
            if (currentParent != null && currentParent != gameStack) {
                if (currentParent instanceof javafx.scene.layout.Pane) {
                    ((javafx.scene.layout.Pane) currentParent).getChildren().remove(settingsPanel);
                }
            }
            // Ensure it's in gameStack
            if (!gameStack.getChildren().contains(settingsPanel)) {
                gameStack.getChildren().add(settingsPanel);
            }
            // Ensure it's properly configured
            settingsPanel.setVisible(false);
            settingsPanel.setManaged(true);
        }
        
        // Remove scene event filters from multiplayer mode
        if (gameBoard != null && gameBoard.getScene() != null) {
            javafx.scene.Scene scene = gameBoard.getScene();
            javafx.event.EventHandler<KeyEvent> sceneKeyPressedHandler = guiController.getSceneKeyPressedHandler();
            javafx.event.EventHandler<KeyEvent> sceneKeyReleasedHandler = guiController.getSceneKeyReleasedHandler();
            if (sceneKeyPressedHandler != null) {
                scene.removeEventFilter(KeyEvent.KEY_PRESSED, sceneKeyPressedHandler);
            }
            if (sceneKeyReleasedHandler != null) {
                scene.removeEventFilter(KeyEvent.KEY_RELEASED, sceneKeyReleasedHandler);
            }
            guiController.setSceneKeyPressedHandler(null);
            guiController.setSceneKeyReleasedHandler(null);
        }
        
        // Restore original panels
        BorderPane rootPane = guiController.getRootBorderPane();
        VBox originalLeftPanel = guiController.getOriginalLeftPanel();
        VBox originalRightPanel = guiController.getOriginalRightPanel();
        if (rootPane != null) {
            if (originalLeftPanel != null) {
                originalLeftPanel.setVisible(true);
                originalLeftPanel.setManaged(true);
                rootPane.setLeft(originalLeftPanel);
            }
            if (originalRightPanel != null) {
                originalRightPanel.setVisible(true);
                originalRightPanel.setManaged(true);
                rootPane.setRight(originalRightPanel);
            }
        }
        // Show single player game board
        if (gameBoard != null) {
            gameBoard.setVisible(true);
            gameBoard.setManaged(true);
            gameBoard.setFocusTraversable(true);
            // Restore node-level keyboard handlers for single player mode
            gameBoard.setOnKeyPressed(e -> inputHandler.handleKeyPress(e, gameStateManager.isMultiplayerMode(), gameStateManager.isGameStarted()));
            gameBoard.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e, gameStateManager.isMultiplayerMode()));
        }
        
        // Restore game panel visibility (grid background) for main menu
        if (gamePanel != null) {
            gamePanel.setVisible(true);
        }
        
        // Show single player panels
        if (holdBrickPanel != null) {
            holdBrickPanel.setVisible(true);
        }
        // Keep next bricks panel container visible, but hide the brick previews
        if (nextBricksPanel != null && singlePlayerScreen != null) {
            nextBricksPanel.setVisible(true);
            // Hide the individual brick panes when main menu is visible
            List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
            if (nextBrickPanes != null) {
                for (GridPane pane : nextBrickPanes) {
                    if (pane != null) {
                        pane.setVisible(false);
                    }
                }
            }
        }
        
        javafx.scene.control.Label scoreLabel = guiController.getScoreLabel();
        javafx.scene.control.Label levelLabel = guiController.getLevelLabel();
        javafx.scene.control.Label linesLabel = guiController.getLinesLabel();
        if (scoreLabel != null) {
            scoreLabel.setVisible(true);
        }
        if (levelLabel != null) {
            levelLabel.setVisible(true);
        }
        if (linesLabel != null) {
            linesLabel.setVisible(true);
        }
        
        // Ensure single player eventListener exists - if not, create a new GameController
        // This handles the case where single player was never initialized or was cleared
        InputEventListener eventListener = guiController.getEventListener();
        if (eventListener == null) {
            new GameController(guiController);
            // Hide next brick panes after GameController creation (it may have called updateNextBricks)
            if (singlePlayerScreen != null) {
                List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
                if (nextBrickPanes != null) {
                    for (GridPane pane : nextBrickPanes) {
                        if (pane != null) {
                            pane.setVisible(false);
                        }
                    }
                }
            }
        } else {
            // If eventListener exists, rebind level and lines to ensure they work
            // This is needed in case the bindings were broken
            if (eventListener instanceof GameController) {
                GameController gameController = (GameController) eventListener;
                if (gameController.getBoard() instanceof SimpleBoard) {
                    SimpleBoard simpleBoard = (SimpleBoard) gameController.getBoard();
                    guiController.bindLevel(simpleBoard.levelProperty(), 0);
                    guiController.bindLines(simpleBoard.linesProperty(), 0);
                }
            }
        }
        
        // Request focus on gameBoard so it can receive keyboard events
        Platform.runLater(() -> {
            if (gameBoard != null) {
                gameBoard.requestFocus();
            }
        });
    }
}

