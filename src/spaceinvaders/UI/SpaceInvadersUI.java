package spaceinvaders.UI;

import spaceinvaders.DataHandlers.MusicHandler;
import spaceinvaders.DataHandlers.MenuImplementations.ThemeImplementation;
import spaceinvaders.GameCalculator;
import spaceinvaders.ListenerActions;
import spaceinvaders.scores.ScoreManager;
import spaceinvaders.characters.Bullet;
import spaceinvaders.characters.Explosion;
import spaceinvaders.characters.Invader;
import spaceinvaders.characters.DeathEffect;
import spaceinvaders.UI.JMenus.ShooterMenu;
import spaceinvaders.UI.JMenus.InvaderMenu;
import spaceinvaders.UI.JMenus.BulletMenu;
import spaceinvaders.UI.JMenus.MusicMenu;
import spaceinvaders.UI.JMenus.BackgroundMenu;
import spaceinvaders.UI.JMenus.EffectsMenu;
import spaceinvaders.UI.JMenus.ThemesMenu;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SpaceInvadersUI extends JPanel implements KeyListener {
    private static SpaceInvadersUI activeInstance;
    private static final String DEATH_SOUND_EFFECT_PATH = "/resources/SoundEffects/player_death.wav";
    private static final String RICK_THEME_PATH = "/resources/Themes/Rick.json";
    private static final String RICK_ROLL_MUSIC_PATH = "/resources/Music/NeverGonnaGiveYouUp.wav";
    private static final String RICK_ROLL_MUSIC_FALLBACK_PATH = "/resources/Music/Retro.wav";
    private static final String RICK_ROLL_BACKGROUND_PATH = "/resources/Background/RickAstleyDance.gif";
    private static final String RICK_ROLL_BACKGROUND_FALLBACK_PATH = "/resources/Background/peterWriting.gif";

    private final Timer repaintTimer;
    public ArrayList<Invader> invaders;
    public ArrayList<Bullet> bullets;
    public ArrayList<Explosion> explosions;
    public ArrayList<DeathEffect> deathEffects;
    public Random random;
    public boolean moveLeft, moveRight;
    public boolean fireHeld;
    private boolean explosionsEnabled = true;
    private final ListenerActions listenerActions;
    public final ImageSelection imageSelection;
    private final PaintingActions paintingActions;
    private int shooter_width = 50;
    private int shooter_height = 60;
    private int shooter_X_Coordinate = 200;
    private GameCalculator gameCalculator;
    private MusicHandler musicHandler;
    public static int breakpointcounter = 0;
    private ScoreManager scoreManager;
    private int playerHealth = 3;
    private boolean playerFlashing = false;
    private long playerFlashStartTime = 0;
    private static final long PLAYER_FLASH_DURATION = 300;
    private boolean gameOver = false;
    private boolean deathSoundPlayed = false;
    private String deathSoundEffectPath = DEATH_SOUND_EFFECT_PATH;
    private long gameOverFlashStartTime = 0;
    private static final long GAME_OVER_FLASH_DURATION = 500; // Flash for 500ms after game over
    private boolean paused = false;

    // Constructor
    public SpaceInvadersUI() {
        activeInstance = this;

        // Timer is used only for repainting (UI thread)
        repaintTimer = new Timer(20, e -> repaint()); // 20ms delay for smoother animations
        // GameCalculator will be started after UI is fully initialized
        invaders = new ArrayList<>(); // Need to describe what ArrayList<> is.
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
        deathEffects = new ArrayList<>();
        random = new Random();
        moveLeft = false;
        moveRight = false;
        fireHeld = false;
        listenerActions = new ListenerActions();
        imageSelection = new ImageSelection();
        paintingActions = new PaintingActions();
        scoreManager = new ScoreManager();
        musicHandler = new MusicHandler();
        musicHandler.start();
        // For debugging

        // Set images
        imageSelection.setGameImages();

        setFocusable(true);
        addKeyListener(this);
        
        // Add ComponentListener to detect when the panel is properly sized
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (getWidth() > 0 && getHeight() > 0) {
                    imageSelection.enableStarsBackground(SpaceInvadersUI.this);
                    repaint();
                    // Now that UI is initialized, start the game calculator thread
                    gameCalculator = new GameCalculator(SpaceInvadersUI.this);
                    gameCalculator.start();
                    scoreManager.start(); // Start the score manager thread
                    removeComponentListener(this); // Remove listener after starting
                }
            }
        });
        
        // Start the repaint timer (UI thread)
        repaintTimer.start();
    }

    public static SpaceInvadersUI getActiveInstance() {
        return activeInstance;
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        listenerActions.keyPressed(e, this);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        listenerActions.keyReleased(e, this);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used. Not providing an implementation Violates Interface Segregation
        // Principle
        // Could be used for character keys.
    }

    @Override
    // Let's move these methods into a separate PaintUI class
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintingActions.drawBackground(g, this);

        if (gameOver) {
            drawGameOver(g);
            return;
        }
        
        if (paused) {
            drawPauseScreen(g);
            return;
        }

        // Draw shooter (rectangle)
        paintingActions.drawShooter(g, this);

        // Draw falling invaders (as images)
        paintingActions.drawInvaders(g, this, imageSelection.getInvaderImage());

        // Draw invader explosion effects
        paintingActions.drawExplosions(g, this);

        // Draw invader death-skin flash/fade effects
        paintingActions.drawDeathEffects(g, this);

        // Draw bullets (bullets)
        paintingActions.drawBullets(g, this);

        // Draw player health hearts
        paintingActions.drawPlayerHealth(g, this);

        // Draw current score
        paintingActions.drawCurrentScore(g, this);
    }

    public int getShooterWidth() {
        return (shooter_width);
    }

    public int getShooterHeight() {
        return (shooter_height);
    }

    public int getShooter_X_Coordinate() {
        return (shooter_X_Coordinate);
    }

    public void setShooter_X_Coordinate(int shooter_X) {
        shooter_X_Coordinate = shooter_X;
    }

    /**
     * Creates and returns the menu bar for the game.
     * @return the JMenuBar with game menus
     */
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(new ShooterMenu());
        menuBar.add(new InvaderMenu());
        menuBar.add(new BulletMenu());
        menuBar.add(new MusicMenu());
        menuBar.add(new BackgroundMenu());
        menuBar.add(new EffectsMenu());
        menuBar.add(new ThemesMenu());
        return menuBar;
    }

    /**
     * Adds points to the current score.
     * 
     * @param points the number of points to add
     */
    public void addPoints(int points) {
        scoreManager.addPoints(points);
    }

    /**
     * Gets the ScoreManager instance.
     * 
     * @return the ScoreManager
     */
    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public MusicHandler getMusicHandler() {
        return musicHandler;
    }

    public void setDeathSoundEffectPath(String deathSoundEffectPath) {
        if (deathSoundEffectPath == null || deathSoundEffectPath.isBlank()) {
            this.deathSoundEffectPath = DEATH_SOUND_EFFECT_PATH;
            return;
        }
        this.deathSoundEffectPath = deathSoundEffectPath;
    }

    public String getDefaultDeathSoundEffectPath() {
        return DEATH_SOUND_EFFECT_PATH;
    }

    public void handleRickRollKill() {
        if (SpaceInvadersUI.class.getResource(RICK_THEME_PATH) != null) {
            ThemeImplementation.requestThemeChange(this, RICK_THEME_PATH);
            return;
        }

        String musicPath = resolveExistingResource(RICK_ROLL_MUSIC_PATH, RICK_ROLL_MUSIC_FALLBACK_PATH);
        if (musicPath != null && musicHandler != null) {
            musicHandler.selectTrack(musicPath);
        }

        String backgroundPath = resolveExistingResource(RICK_ROLL_BACKGROUND_PATH, RICK_ROLL_BACKGROUND_FALLBACK_PATH);
        if (backgroundPath != null) {
            imageSelection.setBackgroundImageFromResourcePath(backgroundPath);
        }

        repaint();
    }

    public boolean isExplosionsEnabled() {
        return explosionsEnabled;
    }

    public void setExplosionsEnabled(boolean explosionsEnabled) {
        this.explosionsEnabled = explosionsEnabled;
        if (!explosionsEnabled) {
            synchronized (this) {
                explosions.clear();
            }
        }
    }

    public int getPlayerHealth() {
        return playerHealth;
    }

    public void damagePlayer() {
        if (playerHealth <= 0 || gameOver) {
            return;
        }

        playerHealth--;
        playerFlashing = true;
        playerFlashStartTime = System.currentTimeMillis();
        repaint();

        if (playerHealth <= 0) {
            setGameOver(true);
        }
    }

    public boolean isPlayerFlashing() {
        if (playerFlashing && System.currentTimeMillis() - playerFlashStartTime > PLAYER_FLASH_DURATION) {
            playerFlashing = false;
        }
        return playerFlashing;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        if (gameOver && this.gameOver) {
            return;
        }

        this.gameOver = gameOver;
        if (gameOver) {
            fireHeld = false;
            gameOverFlashStartTime = System.currentTimeMillis(); // Record when game ended
            if (gameCalculator != null) {
                gameCalculator.stopThread();
            }
            if (musicHandler != null) {
                musicHandler.stopCurrentTrack();
                if (!deathSoundPlayed) {
                    musicHandler.playOneShotEffect(deathSoundEffectPath);
                    deathSoundPlayed = true;
                }
            }
            // DON'T stop the repaintTimer - we need it to display the game over screen
            repaint();
        }
    }

    private void drawPauseScreen(Graphics g) {
        // Draw semi-transparent overlay to darken background
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw pause text in bright green (smaller font)
        g.setColor(new Color(0, 255, 0)); // Bright green
        g.setFont(new Font("Arial", Font.BOLD, 32));
        FontMetrics fm = g.getFontMetrics();
        String pauseText = "Paused Press P to resume";
        int textWidth = fm.stringWidth(pauseText);
        int textHeight = fm.getAscent();
        g.drawString(pauseText, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2);
    }

    private void drawGameOver(Graphics g) {
        Image deathScreenImage = imageSelection.getDeathScreenImage();
        if (deathScreenImage != null) {
            g.drawImage(deathScreenImage, 0, 0, getWidth(), getHeight(), this);
        }

        // Draw red flash effect for a short duration after game over
        if (System.currentTimeMillis() - gameOverFlashStartTime < GAME_OVER_FLASH_DURATION) {
            g.setColor(new Color(255, 0, 0, 180)); // Semi-transparent red
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, 
                             java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String gameOverText = "GAME OVER";
        int textHeight = fm.getAscent();
        int textWidth = fm.stringWidth(gameOverText);
        int baseX = (getWidth() - textWidth) / 2;
        int baseY = (getHeight() + textHeight) / 2;
        
        // Calculate wave animation
        long timeSinceGameOver = System.currentTimeMillis() - gameOverFlashStartTime;
        double wavePhase = (timeSinceGameOver % 2000) / 2000.0; // 2 second cycle
        
        // Draw each letter with wave effect
        int xOffset = baseX;
        for (int i = 0; i < gameOverText.length(); i++) {
            char c = gameOverText.charAt(i);
            
            // Calculate vertical offset for this letter using sine wave
            double letterPhase = wavePhase * Math.PI * 2 + (i * 0.4);
            double yOffset = Math.sin(letterPhase) * 8; // Wave amplitude of 8 pixels
            
            int charWidth = fm.charWidth(c);
            
            // Draw shadow
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.drawString(String.valueOf(c), xOffset + 3, (int)(baseY + yOffset + 3));
            
            // Draw letter
            g2d.setColor(Color.RED);
            g2d.drawString(String.valueOf(c), xOffset, (int)(baseY + yOffset));
            
            xOffset += charWidth;
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        fm = g2d.getFontMetrics();
        String restartText = "Press R to restart";
        textWidth = fm.stringWidth(restartText);
        int restartX = (getWidth() - textWidth) / 2;
        int restartY = (getHeight() + textHeight) / 2 + 60;
        
        // Draw shadow for restart text
        g2d.setColor(new Color(0, 0, 0, 150)); // Dark shadow
        g2d.drawString(restartText, restartX + 2, restartY + 2);
        
        // Draw restart text
        g2d.setColor(Color.YELLOW);
        g2d.drawString(restartText, restartX, restartY);
    }

    public void restartGame() {
        synchronized (this) {
            invaders.clear();
            bullets.clear();
            explosions.clear();
            deathEffects.clear();
            gameOver = false;
            deathSoundPlayed = false;
            gameOverFlashStartTime = 0; // Reset game over flash timer
            playerHealth = 3;
            playerFlashing = false;
            shooter_X_Coordinate = 200;
            moveLeft = false;
            moveRight = false;
            fireHeld = false;
        }

        // Reset the current score for the new game
        scoreManager.resetScore();

        gameCalculator = new GameCalculator(this);
        gameCalculator.start();
        // Don't restart scoreManager - it's a daemon thread that keeps running
        repaintTimer.start();

        // Resume music that was playing before the game ended
        if (musicHandler != null) {
            musicHandler.resumeTrack();
        }
    }

    /**
     * Gracefully shuts down the game calculator thread and stops the repaint timer.
     * Call this before closing the application.
     */
    public void shutdown() {
        if (gameCalculator != null && gameCalculator.isAlive()) {
            gameCalculator.stopThread();
        }
        if (repaintTimer != null) {
            repaintTimer.stop();
        }
        if (scoreManager != null && scoreManager.isAlive()) {
            scoreManager.stopThread();
        }
        if (musicHandler != null && musicHandler.isAlive()) {
            musicHandler.stopThread();
        }
    }

    private String resolveExistingResource(String preferredPath, String fallbackPath) {
        if (SpaceInvadersUI.class.getResource(preferredPath) != null) {
            return preferredPath;
        }
        if (SpaceInvadersUI.class.getResource(fallbackPath) != null) {
            return fallbackPath;
        }
        return null;
    }

}
