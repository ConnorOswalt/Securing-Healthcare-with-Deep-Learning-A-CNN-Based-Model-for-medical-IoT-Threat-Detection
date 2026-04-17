package spaceinvaders;

import spaceinvaders.scores.ScoreManager;
import spaceinvaders.characters.Bullet;
import spaceinvaders.characters.Invader;
import spaceinvaders.JMenus.ParentMenu;
import spaceinvaders.JMenus.ShooterMenu;
import spaceinvaders.JMenus.InvaderMenu;
import spaceinvaders.JMenus.BulletMenu;
import spaceinvaders.JMenus.MusicMenu;

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
    public Random random;
    public boolean moveLeft, moveRight;
    private final ListenerActions listenerActions;
    public final ImageSelection imageSelection;
    private final PaintingActions paintingActions;
    private int shooter_width = 50;
    private int shooter_height = 60;
    private int shooter_X_Coordinate = 200;
    private GameCalculator gameCalculator;
    public static int breakpointcounter = 0;
    private ScoreManager scoreManager;
    private int playerHealth = 3;
    private boolean playerFlashing = false;
    private long playerFlashStartTime = 0;
    private static final long PLAYER_FLASH_DURATION = 300;
    private boolean gameOver = false;

    // Constructor
    public SpaceInvadersUI() {
        activeInstance = this;

        // Timer is used only for repainting (UI thread)
        repaintTimer = new Timer(20, e -> repaint()); // 20ms delay for smoother animations
        // GameCalculator will be started after UI is fully initialized
        invaders = new ArrayList<>(); // Need to describe what ArrayList<> is.
        bullets = new ArrayList<>();
        random = new Random();
        moveLeft = false;
        moveRight = false;
        listenerActions = new ListenerActions();
        imageSelection = new ImageSelection();
        paintingActions = new PaintingActions();
        scoreManager = new ScoreManager();
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
        setBackground(Color.BLACK);

        if (gameOver) {
            drawGameOver(g);
            return;
        }

        // Draw shooter (rectangle)
        paintingActions.drawShooter(g, this);

        // Draw falling invaders (as images)
        paintingActions.drawInvaders(g, this, imageSelection.getInvaderImage());

        // Draw bullets (bullets)
        paintingActions.drawBullets(g, this);

        // Draw player health hearts
        paintingActions.drawPlayerHealth(g, this);
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
            String playerName = JOptionPane.showInputDialog(null, 
                "Game Over! Enter your name for the leaderboard:", 
                "Player Name");
            if (playerName != null && !playerName.trim().isEmpty()) {
                scoreManager.saveScore(playerName.trim());
            }
            setGameOver(true);
        }
    }

   
        gameCalculator = new GameCalculator(this);
        gameCalculator.start();
        scoreManager.start();
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
    }

}
