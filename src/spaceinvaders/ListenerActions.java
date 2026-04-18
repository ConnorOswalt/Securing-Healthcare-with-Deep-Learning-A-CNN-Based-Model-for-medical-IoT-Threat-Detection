package spaceinvaders;

import spaceinvaders.DataHandlers.MenuImplementations.BulletImplementation;
import spaceinvaders.DataHandlers.MenuImplementations.BackgroundImplementation;
import spaceinvaders.DataHandlers.MenuImplementations.EffectsImplementation;
import spaceinvaders.DataHandlers.MenuImplementations.InvaderImplementation;
import spaceinvaders.DataHandlers.MenuImplementations.MusicImplementation;
import spaceinvaders.DataHandlers.MenuImplementations.ShooterImplementation;
import spaceinvaders.DataHandlers.MenuImplementations.ThemeImplementation;
import spaceinvaders.UI.SpaceInvadersUI;

import java.awt.event.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * ListenerActions handles all keyboard input events.
 * These events are processed on the AWT-EventQueue-0 thread.
 * 
 * Position calculations, spawning, and collision detection
 * are now handled by GameCalculator on a separate thread.
 */
public class ListenerActions {
    private final ShooterImplementation shooterImplementation = new ShooterImplementation();
    private final InvaderImplementation invaderImplementation = new InvaderImplementation();
    private final BulletImplementation bulletImplementation = new BulletImplementation();
    private final MusicImplementation musicImplementation = new MusicImplementation();
    private final BackgroundImplementation backgroundImplementation = new BackgroundImplementation();
    private final EffectsImplementation effectsImplementation = new EffectsImplementation();
    private final ThemeImplementation themeImplementation = new ThemeImplementation();


    public void keyPressed(KeyEvent e, SpaceInvadersUI game) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            game.moveLeft = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            game.moveRight = true;
        }
        if (key == KeyEvent.VK_SPACE) {
            game.fireHeld = true;
        }
        if (key == KeyEvent.VK_R && game.isGameOver()) {
            // Prompt for player name before restarting
            // Use SwingUtilities.invokeLater to ensure dialog appears on EDT
            // Pass the game component as parent for proper focus and positioning
            SwingUtilities.invokeLater(() -> {
                String playerName = JOptionPane.showInputDialog(game, 
                    "Enter your name for the leaderboard:", 
                    "Player Name");
                if (playerName != null && !playerName.trim().isEmpty()) {
                    game.getScoreManager().saveScore(playerName.trim());
                } else {
                    // Save with default name if user cancels
                    game.getScoreManager().saveScore("Anonymous");
                }
                game.restartGame();
            });
        }
    }

    public void keyReleased(KeyEvent e, SpaceInvadersUI game) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            game.moveLeft = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            game.moveRight = false;
        }
        if (key == KeyEvent.VK_SPACE) {
            game.fireHeld = false;
        }
    }

    public ActionListener shooterMenuListener() {
        return e -> shooterImplementation.handleShooterSelection(e);
    }

    public ActionListener invaderMenuListener() {
        return e -> invaderImplementation.handleInvaderSelection(e);
    }

    public ActionListener bulletMenuListener() {
        return e -> bulletImplementation.handleBulletSelection(e);
    }

    public ActionListener musicMenuListener() {
        return e -> musicImplementation.handleMusicSelection(e);
    }

    public ActionListener backgroundMenuListener() {
        return e -> backgroundImplementation.handleBackgroundSelection(e);
    }

    public ActionListener effectsMenuListener() {
        return e -> effectsImplementation.handleEffectsSelection(e);
    }

    public ActionListener themesMenuListener() {
        return e -> themeImplementation.handleThemeSelection(e);
    }
}
