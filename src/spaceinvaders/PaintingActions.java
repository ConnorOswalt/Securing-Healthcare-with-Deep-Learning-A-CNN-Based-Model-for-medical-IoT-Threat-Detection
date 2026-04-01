package spaceinvaders;

import spaceinvaders.characters.Bullet;
import spaceinvaders.characters.Invader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PaintingActions handles all rendering of game objects.
 * This is called on the AWT-EventQueue-0 thread during repainting.
 * Reads shared game state safely using synchronized access.
 */
public class PaintingActions {

    public PaintingActions() {

    }

    public void drawShooter(Graphics g, SpaceInvadersUI game) {
        Image shooter_image = game.imageSelection.getShooterImage();
        int shooter_height = game.getShooterHeight();
        int shooter_width = game.getShooterWidth();
        int shooter_X_Coordinate = game.getShooter_X_Coordinate();
        int shooter_Y_Coordinate = game.getHeight() - shooter_height;

        g.drawImage(shooter_image, shooter_X_Coordinate, shooter_Y_Coordinate, shooter_width, shooter_height, game);

    }

    public void drawInvaders(Graphics g, SpaceInvadersUI game, Image invaderImage) {
        // Create a safe copy of the invaders list to avoid concurrent modification issues
        List<Invader> invadersCopy;
        synchronized (game) {
            invadersCopy = new ArrayList<>(game.invaders);
        }
        
        for (Invader invader : invadersCopy) {
            g.drawImage(invaderImage, invader.getX(), invader.getY(), invader.getSize(),
                    invader.getSize(), game);
        }
    }

    public void drawBullets(Graphics g, SpaceInvadersUI game) {
        // Create a safe copy of the bullets list to avoid concurrent modification issues
        List<Bullet> bulletsCopy;
        synchronized (game) {
            bulletsCopy = new ArrayList<>(game.bullets);
        }
        
        g.setColor(Color.YELLOW);
        for (Bullet bullet : bulletsCopy) {
            // Make the bullet into a triangle. Remember where the origin is on the game
            int[] xPoints = { bullet.getX(), bullet.getX() - 5, bullet.getX() + 5 };
            int[] yPoints = { bullet.getY(), bullet.getY() + 10, bullet.getY() + 10 };
            g.fillPolygon(xPoints, yPoints, 3);
        }
    }
}
