package com.comp2042.controller.manager;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

/**
 * Manages all audio operations for the game, including loading, playing, and controlling
 * various sound effects and background music. This class handles audio files for game events
 * such as countdown, line clears, game over, winner announcements, button clicks, hover effects,
 * garbage blocks, and level ups. It also manages background music for the main menu and gameplay.
 * The AudioManager provides volume control, mute functionality, and ensures proper resource
 * management for all MediaPlayer instances. Follows Single Responsibility Principle by
 * exclusively handling audio playback operations.
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
     * Initializes all audio players by loading audio files from the resources directory.
     * Loads all sound effects and music tracks, sets their initial volume, and prepares
     * them for playback. If any audio file fails to load, an error message is printed
     * but initialization continues for other files.
     * 
     * @throws Exception if a critical error occurs during audio file loading
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
    
    /**
     * Loads the countdown sound effect from resources.
     * Creates a MediaPlayer instance for the 3-2-1 countdown audio file.
     */
    private void loadCountdownSound() {
        URL countdownUrl = getClass().getClassLoader().getResource("audio/3-2-1-countdown.mp3");
        if (countdownUrl != null) {
            Media countdownMedia = new Media(countdownUrl.toExternalForm());
            countdownSound = new MediaPlayer(countdownMedia);
        }
    }
    
    /**
     * Loads the game background music from resources.
     * Creates a MediaPlayer instance for the game music and sets it to loop indefinitely.
     */
    private void loadGameMusic() {
        URL gameMusicUrl = getClass().getClassLoader().getResource("audio/A-Type Music (Korobeiniki).mp3");
        if (gameMusicUrl != null) {
            Media gameMusicMedia = new Media(gameMusicUrl.toExternalForm());
            gameMusic = new MediaPlayer(gameMusicMedia);
            gameMusic.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }
    
    /**
     * Loads the game over sound effect from resources.
     * Creates a MediaPlayer instance for the game over audio file.
     */
    private void loadGameOverSound() {
        URL gameOverUrl = getClass().getClassLoader().getResource("audio/Game Over.mp3");
        if (gameOverUrl != null) {
            Media gameOverMedia = new Media(gameOverUrl.toExternalForm());
            gameOverSound = new MediaPlayer(gameOverMedia);
        }
    }
    
    /**
     * Loads the line clear sound effect from resources.
     * Creates a MediaPlayer instance for the line clear audio file.
     */
    private void loadLineClearSound() {
        URL lineClearUrl = getClass().getClassLoader().getResource("audio/Stage Clear.mp3");
        if (lineClearUrl != null) {
            Media lineClearMedia = new Media(lineClearUrl.toExternalForm());
            lineClearSound = new MediaPlayer(lineClearMedia);
        }
    }
    
    /**
     * Loads the main menu background music from resources.
     * Creates a MediaPlayer instance for the main menu music and sets it to loop indefinitely.
     */
    private void loadMainMenuMusic() {
        URL mainMenuUrl = getClass().getClassLoader().getResource("audio/tetris-party-deluxe-main-menu-music.mp3");
        if (mainMenuUrl != null) {
            Media mainMenuMedia = new Media(mainMenuUrl.toExternalForm());
            mainMenuMusic = new MediaPlayer(mainMenuMedia);
            mainMenuMusic.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }
    
    /**
     * Loads the winner sound effect from resources.
     * Creates a MediaPlayer instance for the winner announcement audio file.
     */
    private void loadWinnerSound() {
        URL winnerUrl = getClass().getClassLoader().getResource("audio/winners_W9Cpenj.mp3");
        if (winnerUrl != null) {
            Media winnerMedia = new Media(winnerUrl.toExternalForm());
            winnerSound = new MediaPlayer(winnerMedia);
        }
    }
    
    /**
     * Loads the button click sound effect from resources.
     * Creates a MediaPlayer instance for the button click audio file.
     */
    private void loadClickButtonSound() {
        URL clickButtonUrl = getClass().getClassLoader().getResource("audio/click-button.mp3");
        if (clickButtonUrl != null) {
            Media clickButtonMedia = new Media(clickButtonUrl.toExternalForm());
            clickButtonSound = new MediaPlayer(clickButtonMedia);
        }
    }
    
    /**
     * Loads the garbage block sound effect from resources.
     * Creates a MediaPlayer instance for the garbage block audio file.
     */
    private void loadGarbageSound() {
        URL garbageUrl = getClass().getClassLoader().getResource("audio/garbage.mp3");
        if (garbageUrl != null) {
            Media garbageMedia = new Media(garbageUrl.toExternalForm());
            garbageSound = new MediaPlayer(garbageMedia);
        }
    }
    
    /**
     * Loads the hover sound effect from resources.
     * Creates a MediaPlayer instance for the hover audio file.
     */
    private void loadHoverSound() {
        URL hoverUrl = getClass().getClassLoader().getResource("audio/hover.mp3");
        if (hoverUrl != null) {
            Media hoverMedia = new Media(hoverUrl.toExternalForm());
            hoverSound = new MediaPlayer(hoverMedia);
        }
    }
    
    /**
     * Loads the level up sound effect from resources.
     * Creates a MediaPlayer instance for the level up audio file.
     */
    private void loadLevelUpSound() {
        URL levelUpUrl = getClass().getClassLoader().getResource("audio/level-up.mp3");
        if (levelUpUrl != null) {
            Media levelUpMedia = new Media(levelUpUrl.toExternalForm());
            levelUpSound = new MediaPlayer(levelUpMedia);
        }
    }
    
    /**
     * Sets the volume for all audio players.
     * Updates the current volume setting and applies it to all MediaPlayer instances
     * if the audio is not muted. The volume change takes effect immediately for all
     * currently loaded audio players.
     * 
     * @param volume Volume level between 0.0 and 1.0 (0.0 is silent, 1.0 is maximum)
     * @throws IllegalArgumentException if volume is outside the valid range [0.0, 1.0]
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
     * Gets the current volume level setting.
     * Returns the stored volume value, which may differ from the actual playback volume
     * if the audio is currently muted.
     * 
     * @return Current volume level between 0.0 and 1.0
     */
    public double getVolume() {
        return currentVolume;
    }
    
    /**
     * Toggles the mute state of all audio playback.
     * When muting, sets all audio players to volume 0.0. When unmuting, restores
     * all audio players to the previously set volume level.
     * 
     * @return true if audio is now muted, false if audio is now unmuted
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
     * 
     * @return true if audio is muted, false if audio is playing at the set volume level
     */
    public boolean isMuted() {
        return isMuted;
    }
    
    /**
     * Applies the specified volume level to all loaded MediaPlayer instances.
     * Iterates through all audio players and sets their volume, skipping any
     * players that have not been initialized.
     * 
     * @param volume The volume level to apply (between 0.0 and 1.0)
     */
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
     * Plays the countdown sound effect.
     * Stops any currently playing countdown sound, resets it to the beginning,
     * and starts playback. Does nothing if the countdown sound has not been loaded.
     */
    public void playCountdown() {
        if (countdownSound != null) {
            countdownSound.stop();
            countdownSound.seek(Duration.ZERO);
            countdownSound.play();
        }
    }
    
    /**
     * Plays the game background music in a continuous loop.
     * Stops the main menu music if it is playing, then starts the game music.
     * The music will loop indefinitely until stopped. Does nothing if the game
     * music has not been loaded.
     */
    public void playGameMusic() {
        if (gameMusic != null) {
            stopMainMenuMusic();
            gameMusic.play();
        }
    }
    
    /**
     * Stops the game background music playback.
     * Does nothing if the game music has not been loaded or is not currently playing.
     */
    public void stopGameMusic() {
        if (gameMusic != null) {
            gameMusic.stop();
        }
    }
    
    /**
     * Plays the game over sound effect.
     * Stops any currently playing game over sound, resets it to the beginning,
     * and starts playback. Does nothing if the game over sound has not been loaded.
     */
    public void playGameOver() {
        if (gameOverSound != null) {
            gameOverSound.stop();
            gameOverSound.seek(Duration.ZERO);
            gameOverSound.play();
        }
    }
    
    /**
     * Plays the line clear sound effect.
     * Stops any currently playing line clear sound, resets it to the beginning,
     * and starts playback. Does nothing if the line clear sound has not been loaded.
     */
    public void playLineClear() {
        if (lineClearSound != null) {
            lineClearSound.stop();
            lineClearSound.seek(Duration.ZERO);
            lineClearSound.play();
        }
    }
    
    /**
     * Plays the main menu background music in a continuous loop.
     * Stops the game music if it is currently playing, then starts the main menu music.
     * The music will loop indefinitely until stopped. Does nothing if the main menu
     * music has not been loaded.
     */
    public void playMainMenuMusic() {
        if (mainMenuMusic != null) {
            stopGameMusic();
            mainMenuMusic.play();
        }
    }
    
    /**
     * Stops the main menu background music playback.
     * Does nothing if the main menu music has not been loaded or is not currently playing.
     */
    public void stopMainMenuMusic() {
        if (mainMenuMusic != null) {
            mainMenuMusic.stop();
        }
    }
    
    /**
     * Plays the winner announcement sound effect.
     * Stops any currently playing winner sound, resets it to the beginning,
     * and starts playback. Does nothing if the winner sound has not been loaded.
     */
    public void playWinner() {
        if (winnerSound != null) {
            winnerSound.stop();
            winnerSound.seek(Duration.ZERO);
            winnerSound.play();
        }
    }
    
    /**
     * Plays the button click sound effect.
     * Stops any currently playing button click sound, resets it to the beginning,
     * and starts playback. Does nothing if the button click sound has not been loaded.
     */
    public void playClickButton() {
        if (clickButtonSound != null) {
            clickButtonSound.stop();
            clickButtonSound.seek(Duration.ZERO);
            clickButtonSound.play();
        }
    }
    
    /**
     * Plays the garbage block sound effect.
     * Stops any currently playing garbage sound, resets it to the beginning,
     * and starts playback. Does nothing if the garbage sound has not been loaded.
     */
    public void playGarbage() {
        if (garbageSound != null) {
            garbageSound.stop();
            garbageSound.seek(Duration.ZERO);
            garbageSound.play();
        }
    }
    
    /**
     * Plays the hover sound effect (typically used for UI button hover events).
     * Stops any currently playing hover sound, resets it to the beginning,
     * and starts playback. Does nothing if the hover sound has not been loaded.
     */
    public void playHover() {
        if (hoverSound != null) {
            hoverSound.stop();
            hoverSound.seek(Duration.ZERO);
            hoverSound.play();
        }
    }
    
    /**
     * Plays the level up sound effect.
     * Stops any currently playing level up sound, resets it to the beginning,
     * and starts playback. Does nothing if the level up sound has not been loaded.
     */
    public void playLevelUp() {
        if (levelUpSound != null) {
            levelUpSound.stop();
            levelUpSound.seek(Duration.ZERO);
            levelUpSound.play();
        }
    }
    
    /**
     * Stops all audio playback for all loaded MediaPlayer instances.
     * This includes all sound effects and background music. Does nothing for
     * audio players that have not been loaded or are not currently playing.
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
     * Cleans up all audio resources when the audio manager is no longer needed.
     * Stops all audio playback and releases all MediaPlayer references to allow
     * garbage collection. Should be called when the AudioManager is being destroyed
     * to prevent resource leaks.
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

