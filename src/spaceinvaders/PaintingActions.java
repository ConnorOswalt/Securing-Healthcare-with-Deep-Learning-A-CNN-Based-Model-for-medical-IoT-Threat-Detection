package spaceinvaders;

import spaceinvaders.JMenus.MenuImplementations.BulletImplementation;
import spaceinvaders.characters.Bullet;
import spaceinvaders.characters.Explosion;
import spaceinvaders.characters.Invader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * PaintingActions handles all rendering of game objects.
 * This is called on the AWT-EventQueue-0 thread during repainting.
 * Reads shared game state safely using synchronized access.
 */
public class PaintingActions {
    public PaintingActions() {

    }

    public void drawBackground(Graphics g, SpaceInvadersUI game) {
        Image backgroundImage = game.imageSelection.getBackgroundImage();
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, game.getWidth(), game.getHeight(), game);
            return;
        }

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, game.getWidth(), game.getHeight());
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

    public void drawExplosions(Graphics g, SpaceInvadersUI game) {
        List<Explosion> explosionsCopy;
        synchronized (game) {
            explosionsCopy = new ArrayList<>(game.explosions);
        }

        for (Explosion explosion : explosionsCopy) {
            double progress = explosion.getProgress();
            int radius = (int) Math.max(2, explosion.getMaxRadius() * progress);
            int alpha = (int) Math.max(0, 255 * (1.0 - progress));

            g.setColor(new Color(255, 170, 0, alpha));
            g.fillOval(explosion.getCenterX() - radius, explosion.getCenterY() - radius,
                    radius * 2, radius * 2);

            int innerRadius = Math.max(2, radius / 2);
            g.setColor(new Color(255, 240, 120, alpha));
            g.fillOval(explosion.getCenterX() - innerRadius, explosion.getCenterY() - innerRadius,
                    innerRadius * 2, innerRadius * 2);
        }
    }

    public void drawBullets(Graphics g, SpaceInvadersUI game) {
        // Create a safe copy of the bullets list to avoid concurrent modification issues
        List<Bullet> bulletsCopy;
        synchronized (game) {
            bulletsCopy = new ArrayList<>(game.bullets);
        }
<<<<<<< HEAD
        
        Image bulletImage = game.imageSelection.getBulletImage();
        for (Bullet bullet : bulletsCopy) {
            if (bulletImage != null) {
                // Render image/gif centred on bullet x, tip at bullet y
                g.drawImage(bulletImage, bullet.getX() - 12, bullet.getY(), 24, 40, game);
            } else {
                // Fallback: yellow triangle
                g.setColor(Color.YELLOW);
                int[] xPoints = { bullet.getX(), bullet.getX() - 12, bullet.getX() + 12 };
                int[] yPoints = { bullet.getY(), bullet.getY() + 20, bullet.getY() + 20 };
                g.fillPolygon(xPoints, yPoints, 3);
=======

        String selectedType = BulletImplementation.getSelectedBulletType();
        String selectedPath = BulletImplementation.getSelectedBulletPath();

        for (Bullet bullet : bulletsCopy) {
            String mode = selectedType == null ? "Triangle" : selectedType;
            switch (mode) {
                case "Triangle":
                    g.setColor(Color.YELLOW);
                    int[] triangleXPoints = { bullet.getX(), bullet.getX() - 5, bullet.getX() + 5 };
                    int[] triangleYPoints = { bullet.getY(), bullet.getY() + 10, bullet.getY() + 10 };
                    g.fillPolygon(triangleXPoints, triangleYPoints, 3);
                    break;
                case "Circle":
                    g.setColor(Color.RED);
                    g.fillOval(bullet.getX() - 5, bullet.getY(), 10, 10);
                    break;
                default:
                    try {
                        Image bulletImage = loadBulletImage(selectedPath);
                        if (bulletImage != null) {
                            int drawWidth = 24;
                            int drawHeight = 32;
                            int drawX = bullet.getX() - (drawWidth / 2);
                            int drawY = bullet.getY() - drawHeight;
                            g.drawImage(bulletImage, drawX, drawY, drawWidth, drawHeight, game);
                            break;
                        }
                    } catch (IOException | IllegalArgumentException e) {
                        // Fall back to triangle if image cannot be loaded.
                    }

                    g.setColor(Color.YELLOW);
                    int[] fallbackXPoints = { bullet.getX(), bullet.getX() - 5, bullet.getX() + 5 };
                    int[] fallbackYPoints = { bullet.getY(), bullet.getY() + 10, bullet.getY() + 10 };
                    g.fillPolygon(fallbackXPoints, fallbackYPoints, 3);
                    break;
>>>>>>> origin/ScoreSystem
            }
        }
    }

    private Image loadBulletImage(String selectedPath) throws IOException {
        if (selectedPath == null || selectedPath.isBlank()) {
            return null;
        }

        // Try classpath lookup first.
        java.net.URL resourceUrl = PaintingActions.class.getResource(selectedPath);
        if (resourceUrl == null && selectedPath.startsWith("/")) {
            resourceUrl = PaintingActions.class.getClassLoader().getResource(selectedPath.substring(1));
        }
        if (resourceUrl != null) {
            return ImageIO.read(resourceUrl);
        }

        // Fallback for runs where resources are not copied to output folder.
        String normalized = selectedPath.replace('\\', '/');
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        File sourceResourceFile = new File("src", normalized);
        if (sourceResourceFile.exists()) {
            return ImageIO.read(sourceResourceFile);
        }

        return null;
    }

    public void drawPlayerHealth(Graphics g, SpaceInvadersUI game) {
        int playerHealth = game.getPlayerHealth();
        int heartSize = 16;
        int startX = 10;
        int startY = 10;

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Player Hearts:", startX, startY + heartSize);

        for (int i = 0; i < 3; i++) {
            int x = startX + i * (heartSize + 10);
            int y = startY + 20;
            if (i < playerHealth) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.GRAY);
            }

            int[] xPoints = {x + heartSize/2, x, x + heartSize/4, x + heartSize/2, x + 3*heartSize/4, x + heartSize};
            int[] yPoints = {y + heartSize, y + heartSize/2, y, y, y, y + heartSize/2};
            g.fillPolygon(xPoints, yPoints, 6);
        }

        // Draw flashing effect when player is hit
        if (game.isPlayerFlashing()) {
            g.setColor(new Color(255, 0, 0, 128)); // Semi-transparent red
            g.fillRect(0, 0, game.getWidth(), game.getHeight());
        }
    }

    public void drawCurrentScore(Graphics g, SpaceInvadersUI game) {
        int score = game.getScoreManager().getCurrentScore();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String scoreText = String.format("Score: %03d", score);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(scoreText);
        g.drawString(scoreText, game.getWidth() - textWidth - 10, 20);
    }
}
