package spaceinvaders;

import spaceinvaders.characters.Bullet;
import spaceinvaders.characters.Invader;

import java.awt.event.*;

/**
 * ListenerActions handles all keyboard input events.
 * These events are processed on the AWT-EventQueue-0 thread.
 * 
 * Position calculations, spawning, and collision detection
 * are now handled by GameCalculator on a separate thread.
 */
public class ListenerActions {

    public void keyPressed(KeyEvent e, SpaceInvadersUI game) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            game.moveLeft = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            game.moveRight = true;
        }
        if (key == KeyEvent.VK_SPACE) {
            synchronized (game) {
                int shooter_X_Coordinate = game.getShooter_X_Coordinate();
                int shooter_width = game.getShooterWidth();
                int shooter_height = game.getShooterHeight();
                game.bullets.add(
                        new Bullet(shooter_X_Coordinate + shooter_width / 2, game.getHeight() - shooter_height));
            }
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
    }
}
