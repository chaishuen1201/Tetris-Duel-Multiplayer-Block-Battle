package com.comp2042.controller.manager;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

/**
 * Manages all audio operations for the game.
 * Follows Single Responsibility Principle - only handles audio playback.
 */
public class AudioManager {
    
    private static final double DEFAULT_VOLUME = 0.5;
    
    private MediaPlayer countdownSound;
    private MediaPlayer gameMusic;
    private MediaPlayer gameOverSound;
    private MediaPlayer lineClearSound;
    private MediaPlayer mainMenuMusic;
    private MediaPlayer winnerSound;
    private MediaPlayer clickButtonSound;
    private MediaPlayer garbageSound;
    private MediaPlayer hoverSound;
    private MediaPlayer levelUpSound;
    
    private double currentVolume = DEFAULT_VOLUME;
    private boolean isMuted = false;
    
    /**
     * Initializes all audio players by loading audio files.
     */
    public void initialize() {
        try {
            loadCountdownSound();
            loadGameMusic();
            loadGameOverSound();
            loadLineClearSound();
            loadMainMenuMusic();
            loadWinnerSound();
            loadClickButtonSound();
            loadGarbageSound();
            loadHoverSound();
            loadLevelUpSound();
            applyVolumeToAllPlayers(currentVolume);
        } catch (Exception e) {
            System.err.println("Error loading audio files: " + e.getMessage());
        }
    }
    
    private void loadCountdownSound() {
        URL countdownUrl = getClass().getClassLoader().getResource("audio/3-2-1-countdown.mp3");
        if (countdownUrl != null) {
            Media countdownMedia = new Media(countdownUrl.toExternalForm());
            countdownSound = new MediaPlayer(countdownMedia);
        }
    }
    
    private void loadGameMusic() {
        URL gameMusicUrl = getClass().getClassLoader().getResource("audio/A-Type Music (Korobeiniki).mp3");
        if (gameMusicUrl != null) {
            Media gameMusicMedia = new Media(gameMusicUrl.toExternalForm());
            gameMusic = new MediaPlayer(gameMusicMedia);
            gameMusic.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }
    
    private void loadGameOverSound() {
        URL gameOverUrl = getClass().getClassLoader().getResource("audio/Game Over.mp3");
        if (gameOverUrl != null) {
            Media gameOverMedia = new Media(gameOverUrl.toExternalForm());
            gameOverSound = new MediaPlayer(gameOverMedia);
        }
    }
    
    private void loadLineClearSound() {
        URL lineClearUrl = getClass().getClassLoader().getResource("audio/Stage Clear.mp3");
        if (lineClearUrl != null) {
            Media lineClearMedia = new Media(lineClearUrl.toExternalForm());
            lineClearSound = new MediaPlayer(lineClearMedia);
        }
    }
    
    private void loadMainMenuMusic() {
        URL mainMenuUrl = getClass().getClassLoader().getResource("audio/tetris-party-deluxe-main-menu-music.mp3");
        if (mainMenuUrl != null) {
            Media mainMenuMedia = new Media(mainMenuUrl.toExternalForm());
            mainMenuMusic = new MediaPlayer(mainMenuMedia);
            mainMenuMusic.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }
    
    private void loadWinnerSound() {
        URL winnerUrl = getClass().getClassLoader().getResource("audio/winners_W9Cpenj.mp3");
        if (winnerUrl != null) {
            Media winnerMedia = new Media(winnerUrl.toExternalForm());
            winnerSound = new MediaPlayer(winnerMedia);
        }
    }
    
    private void loadClickButtonSound() {
        URL clickButtonUrl = getClass().getClassLoader().getResource("audio/click-button.mp3");
        if (clickButtonUrl != null) {
            Media clickButtonMedia = new Media(clickButtonUrl.toExternalForm());
            clickButtonSound = new MediaPlayer(clickButtonMedia);
        }
    }
    
    private void loadGarbageSound() {
        URL garbageUrl = getClass().getClassLoader().getResource("audio/garbage.mp3");
        if (garbageUrl != null) {
            Media garbageMedia = new Media(garbageUrl.toExternalForm());
            garbageSound = new MediaPlayer(garbageMedia);
        }
    }
    
    private void loadHoverSound() {
        URL hoverUrl = getClass().getClassLoader().getResource("audio/hover.mp3");
        if (hoverUrl != null) {
            Media hoverMedia = new Media(hoverUrl.toExternalForm());
            hoverSound = new MediaPlayer(hoverMedia);
        }
    }
    
    private void loadLevelUpSound() {
        URL levelUpUrl = getClass().getClassLoader().getResource("audio/level-up.mp3");
        if (levelUpUrl != null) {
            Media levelUpMedia = new Media(levelUpUrl.toExternalForm());
            levelUpSound = new MediaPlayer(levelUpMedia);
        }
    }
    
    /**
     * Sets the volume for all audio players.
     * @param volume Volume level between 0.0 and 1.0
     */
    public void setVolume(double volume) {
        if (volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
        }
        this.currentVolume = volume;
        if (!isMuted) {
            applyVolumeToAllPlayers(volume);
        }
    }
    
    /**
     * Gets the current volume level.
     * @return Current volume between 0.0 and 1.0
     */
    public double getVolume() {
        return currentVolume;
    }
    
    /**
     * Toggles mute state.
     * @return true if now muted, false if now unmuted
     */
    public boolean toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            applyVolumeToAllPlayers(0.0);
        } else {
            applyVolumeToAllPlayers(currentVolume);
        }
        return isMuted;
    }
    
    /**
     * Checks if audio is currently muted.
     * @return true if muted, false otherwise
     */
    public boolean isMuted() {
        return isMuted;
    }
    
    private void applyVolumeToAllPlayers(double volume) {
        if (countdownSound != null) {
            countdownSound.setVolume(volume);
        }
        if (gameMusic != null) {
            gameMusic.setVolume(volume);
        }
        if (gameOverSound != null) {
            gameOverSound.setVolume(volume);
        }
        if (lineClearSound != null) {
            lineClearSound.setVolume(volume);
        }
        if (mainMenuMusic != null) {
            mainMenuMusic.setVolume(volume);
        }
        if (winnerSound != null) {
            winnerSound.setVolume(volume);
        }
        if (clickButtonSound != null) {
            clickButtonSound.setVolume(volume);
        }
        if (garbageSound != null) {
            garbageSound.setVolume(volume);
        }
        if (hoverSound != null) {
            hoverSound.setVolume(volume);
        }
        if (levelUpSound != null) {
            levelUpSound.setVolume(volume);
        }
    }
    
    /**
     * Plays the countdown sound.
     */
    public void playCountdown() {
        if (countdownSound != null) {
            countdownSound.stop();
            countdownSound.seek(Duration.ZERO);
            countdownSound.play();
        }
    }
    
    /**
     * Plays the game music (looping).
     */
    public void playGameMusic() {
        if (gameMusic != null) {
            stopMainMenuMusic();
            gameMusic.play();
        }
    }
    
    /**
     * Stops the game music.
     */
    public void stopGameMusic() {
        if (gameMusic != null) {
            gameMusic.stop();
        }
    }
    
    /**
     * Plays the game over sound.
     */
    public void playGameOver() {
        if (gameOverSound != null) {
            gameOverSound.stop();
            gameOverSound.seek(Duration.ZERO);
            gameOverSound.play();
        }
    }
    
    /**
     * Plays the line clear sound.
     */
    public void playLineClear() {
        if (lineClearSound != null) {
            lineClearSound.stop();
            lineClearSound.seek(Duration.ZERO);
            lineClearSound.play();
        }
    }
    
    /**
     * Plays the main menu music (looping).
     */
    public void playMainMenuMusic() {
        if (mainMenuMusic != null) {
            stopGameMusic();
            mainMenuMusic.play();
        }
    }
    
    /**
     * Stops the main menu music.
     */
    public void stopMainMenuMusic() {
        if (mainMenuMusic != null) {
            mainMenuMusic.stop();
        }
    }
    
    /**
     * Plays the winner sound.
     */
    public void playWinner() {
        if (winnerSound != null) {
            winnerSound.stop();
            winnerSound.seek(Duration.ZERO);
            winnerSound.play();
        }
    }
    
    /**
     * Plays the button click sound.
     */
    public void playClickButton() {
        if (clickButtonSound != null) {
            clickButtonSound.stop();
            clickButtonSound.seek(Duration.ZERO);
            clickButtonSound.play();
        }
    }
    
    /**
     * Plays the garbage sound.
     */
    public void playGarbage() {
        if (garbageSound != null) {
            garbageSound.stop();
            garbageSound.seek(Duration.ZERO);
            garbageSound.play();
        }
    }
    
    /**
     * Plays the hover sound.
     */
    public void playHover() {
        if (hoverSound != null) {
            hoverSound.stop();
            hoverSound.seek(Duration.ZERO);
            hoverSound.play();
        }
    }
    
    /**
     * Plays the level up sound.
     */
    public void playLevelUp() {
        if (levelUpSound != null) {
            levelUpSound.stop();
            levelUpSound.seek(Duration.ZERO);
            levelUpSound.play();
        }
    }
    
    /**
     * Stops all audio playback.
     */
    public void stopAll() {
        if (countdownSound != null) {
            countdownSound.stop();
        }
        if (gameMusic != null) {
            gameMusic.stop();
        }
        if (gameOverSound != null) {
            gameOverSound.stop();
        }
        if (lineClearSound != null) {
            lineClearSound.stop();
        }
        if (mainMenuMusic != null) {
            mainMenuMusic.stop();
        }
        if (winnerSound != null) {
            winnerSound.stop();
        }
        if (clickButtonSound != null) {
            clickButtonSound.stop();
        }
        if (garbageSound != null) {
            garbageSound.stop();
        }
        if (hoverSound != null) {
            hoverSound.stop();
        }
        if (levelUpSound != null) {
            levelUpSound.stop();
        }
    }
    
    /**
     * Cleans up resources when audio manager is no longer needed.
     */
    public void dispose() {
        stopAll();
        countdownSound = null;
        gameMusic = null;
        gameOverSound = null;
        lineClearSound = null;
        mainMenuMusic = null;
        winnerSound = null;
        clickButtonSound = null;
        garbageSound = null;
        hoverSound = null;
        levelUpSound = null;
    }
}

