package spaceinvaders;

import spaceinvaders.JMenus.MenuImplementations.BulletImplementation;
import spaceinvaders.characters.Bullet;
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
    private String cachedBulletPath;
    private Image cachedBulletImage;

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

        String selectedType = BulletImplementation.getSelectedBulletType();
        String selectedPath = BulletImplementation.getSelectedBulletPath();
        String mode = selectedType == null ? "Triangle" : selectedType;

        Image bulletImage = null;
        if (!"Triangle".equals(mode) && !"Circle".equals(mode)) {
            try {
                bulletImage = loadBulletImage(selectedPath);
            } catch (IOException | IllegalArgumentException e) {
                bulletImage = null;
            }
        }

        for (Bullet bullet : bulletsCopy) {
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
                    if (bulletImage != null) {
                        int drawWidth = 24;
                        int drawHeight = 32;
                        int drawX = bullet.getX() - (drawWidth / 2);
                        int drawY = bullet.getY() - drawHeight;
                        g.drawImage(bulletImage, drawX, drawY, drawWidth, drawHeight, game);
                        break;
                    }

                    g.setColor(Color.YELLOW);
                    int[] fallbackXPoints = { bullet.getX(), bullet.getX() - 5, bullet.getX() + 5 };
                    int[] fallbackYPoints = { bullet.getY(), bullet.getY() + 10, bullet.getY() + 10 };
                    g.fillPolygon(fallbackXPoints, fallbackYPoints, 3);
                    break;
            }
        }
    }

    private Image loadBulletImage(String selectedPath) throws IOException {
        if (selectedPath == null || selectedPath.isBlank()) {
            cachedBulletPath = null;
            cachedBulletImage = null;
            return null;
        }

        if (selectedPath.equals(cachedBulletPath)) {
            return cachedBulletImage;
        }

        Image loadedImage = null;

        // Try classpath lookup first.
        java.net.URL resourceUrl = PaintingActions.class.getResource(selectedPath);
        if (resourceUrl == null && selectedPath.startsWith("/")) {
            resourceUrl = PaintingActions.class.getClassLoader().getResource(selectedPath.substring(1));
        }
        if (resourceUrl != null) {
            loadedImage = ImageIO.read(resourceUrl);
            cachedBulletPath = selectedPath;
            cachedBulletImage = loadedImage;
            return loadedImage;
        }

        // Fallback for runs where resources are not copied to output folder.
        String normalized = selectedPath.replace('\\', '/');
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        File sourceResourceFile = new File("src", normalized);
        if (sourceResourceFile.exists()) {
            loadedImage = ImageIO.read(sourceResourceFile);
        }

        cachedBulletPath = selectedPath;
        cachedBulletImage = loadedImage;
        return loadedImage;
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
