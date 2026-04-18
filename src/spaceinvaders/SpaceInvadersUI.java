package spaceinvaders;

import spaceinvaders.scores.ScoreManager;
import spaceinvaders.characters.Bullet;
import spaceinvaders.characters.Explosion;
import spaceinvaders.characters.Invader;
import spaceinvaders.JMenus.ShooterMenu;
import spaceinvaders.JMenus.InvaderMenu;
import spaceinvaders.JMenus.BulletMenu;
import spaceinvaders.JMenus.MusicMenu;
import spaceinvaders.JMenus.BackgroundMenu;
import spaceinvaders.JMenus.EffectsMenu;
import spaceinvaders.JMenus.ThemesMenu;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SpaceInvadersUI extends JPanel implements KeyListener {
    private static SpaceInvadersUI activeInstance;

    private final Timer repaintTimer;
    public ArrayList<Invader> invaders;
    public ArrayList<Bullet> bullets;
    public ArrayList<Explosion> explosions;
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
    private long gameOverFlashStartTime = 0;
    private static final long GAME_OVER_FLASH_DURATION = 500; // Flash for 500ms after game over

    // Constructor
    public SpaceInvadersUI() {
        activeInstance = this;

        // Timer is used only for repainting (UI thread)
        repaintTimer = new Timer(20, e -> repaint()); // 20ms delay for smoother animations
        // GameCalculator will be started after UI is fully initialized
        invaders = new ArrayList<>(); // Need to describe what ArrayList<> is.
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
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

        // Draw shooter (rectangle)
        paintingActions.drawShooter(g, this);

        // Draw falling invaders (as images)
        paintingActions.drawInvaders(g, this, imageSelection.getInvaderImage());

        // Draw invader explosion effects
        paintingActions.drawExplosions(g, this);

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
        this.gameOver = gameOver;
        if (gameOver) {
            fireHeld = false;
            gameOverFlashStartTime = System.currentTimeMillis(); // Record when game ended
            if (gameCalculator != null) {
                gameCalculator.stopThread();
            }
            // DON'T stop the repaintTimer - we need it to display the game over screen
            repaint();
        }
    }

    private void drawGameOver(Graphics g) {
        // Draw red flash effect for a short duration after game over
        if (System.currentTimeMillis() - gameOverFlashStartTime < GAME_OVER_FLASH_DURATION) {
            g.setColor(new Color(255, 0, 0, 180)); // Semi-transparent red
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        String gameOverText = "GAME OVER";
        int textWidth = fm.stringWidth(gameOverText);
        int textHeight = fm.getAscent();
        g.drawString(gameOverText, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        fm = g.getFontMetrics();
        String restartText = "Press R to restart";
        textWidth = fm.stringWidth(restartText);
        g.drawString(restartText, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 + 60);
    }

    public void restartGame() {
        synchronized (this) {
            invaders.clear();
            bullets.clear();
            explosions.clear();
            gameOver = false;
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

}
