package spaceinvaders;

import spaceinvaders.characters.Bullet;
import spaceinvaders.characters.Explosion;
import spaceinvaders.characters.Invader;

import java.awt.*;
import java.util.Iterator;

/**
 * GameCalculator runs on a separate thread and handles:
 * - Position updates for the shooter, invaders, and bullets
 * - Spawning of new invaders
 * - Collision detection between bullets and invaders
 * 
 * The UI rendering and event handling remain on the AWT-EventQueue-0 thread.
 */
public class GameCalculator extends Thread {
    private final SpaceInvadersUI game;
    private volatile boolean running = true;
    private static final long UPDATE_INTERVAL_MS = 20; // Same as original timer interval
    private static final long FIRE_INTERVAL_MS = 150;
    private static final long EXPLOSION_DURATION_MS = 300;
    private long lastFireTimeMs = 0;

    public GameCalculator(SpaceInvadersUI game) {
        this.game = game;
        setDaemon(true);
        setName("GameCalculator-Thread");
    }

    @Override
    public void run() {
        long lastUpdateTime = System.currentTimeMillis();
        
        while (running) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - lastUpdateTime;
            
            if (elapsed >= UPDATE_INTERVAL_MS) {
                updateGameState();
                lastUpdateTime = currentTime;
            } else {
                // Sleep to avoid busy waiting
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void updateGameState() {
        updateShooterPosition();
        handleShooting();
        updateInvaderPositions();
        updateBulletPositions();
        checkCollisions();
        updateExplosions();
        spawnNewInvaders();
    }

    private void updateExplosions() {
        synchronized (game) {
            Iterator<Explosion> explosionIterator = game.explosions.iterator();
            while (explosionIterator.hasNext()) {
                if (explosionIterator.next().isExpired()) {
                    explosionIterator.remove();
                }
            }
        }
    }

    private void updateContinuousFire() {
        synchronized (game) {
            if (!game.firing || game.isGameOver()) {
                return;
            }

            long currentTime = System.currentTimeMillis();
    private void handleShooting() {
            if (game.isGameOver() || !game.fireHeld) {
                if (game.isGameOver() || !game.fireHeld) {
            }

            long now = System.currentTimeMillis();
            }

            int shooterWidth = game.getShooterWidth();
            int shooterHeight = game.getShooterHeight();
            game.bullets.add(new Bullet(shooterX + shooterWidth / 2, game.getHeight() - shooterHeight));
            lastFireTimeMs = now;
        }
    }

    private void updateShooterPosition() {
        synchronized (game) {
            int shooter_X_Coordinate = game.getShooter_X_Coordinate();
            int shooter_Width = game.getShooterWidth();
            
            // Move shooter left or right
            if (game.moveLeft && shooter_X_Coordinate > 0) {
                game.setShooter_X_Coordinate(shooter_X_Coordinate - 5);
            }
            if (game.moveRight && shooter_X_Coordinate < game.getWidth() - shooter_Width) {
                game.setShooter_X_Coordinate(shooter_X_Coordinate + 5);
            }
        }
    }

    private void spawnNewInvaders() {
        synchronized (game) {
            if (game.random.nextInt(100) < 2) {
                int x = game.random.nextInt(game.getWidth());
                game.invaders.add(new Invader(x, 0, 40));
            }
        }
    }

    private void updateInvaderPositions() {
        synchronized (game) {
            Iterator<Invader> invaderIterator = game.invaders.iterator();
            while (invaderIterator.hasNext()) {
                Invader invader = invaderIterator.next();
                int y = invader.getY();
                invader.setY(y + 2);
                if (invader.getY() > game.getHeight()) {
                    invaderIterator.remove();
                }
            }
        }
    }

    private void updateBulletPositions() {
        synchronized (game) {
            Iterator<Bullet> bulletIterator = game.bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                int y = bullet.getY();
                bullet.setY(y - 5);
                if (bullet.getY() < 0) {
                    bulletIterator.remove();
                }
            }
        }
    }

    private void checkCollisions() {
        synchronized (game) {
            // Check bullet-invader collisions
            Iterator<Bullet> bulletIterator = game.bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                Iterator<Invader> invaderIterator = game.invaders.iterator();
                while (invaderIterator.hasNext()) {
                    Invader invader = invaderIterator.next();
                    if (new Rectangle(bullet.getX() - 5, bullet.getY(), 10, 10).intersects(
                            new Rectangle(invader.getX(), invader.getY(), invader.getSize(),
                                    invader.getSize()))) {
                        addExplosionForInvader(invader);
                        bulletIterator.remove();
                        invaderIterator.remove();
                        game.addPoints(10);
                        break;
                    }
                }
            }

            // Check shooter-invader collisions
            if (!game.isGameOver()) {
                int shooterX = game.getShooter_X_Coordinate();
                int shooterY = game.getHeight() - game.getShooterHeight();
                int shooterWidth = game.getShooterWidth();
                int shooterHeight = game.getShooterHeight();

                Rectangle shooterRect = new Rectangle(shooterX, shooterY, shooterWidth, shooterHeight);

                Iterator<Invader> invaderIterator = game.invaders.iterator();
                while (invaderIterator.hasNext()) {
                    Invader invader = invaderIterator.next();
                    Rectangle invaderRect = new Rectangle(invader.getX(), invader.getY(),
                            invader.getSize(), invader.getSize());
                    if (shooterRect.intersects(invaderRect)) {
                        addExplosionForInvader(invader);
                        invaderIterator.remove();
                        game.damagePlayer();
                        break; // Only one collision per frame
                    }
                }
            }
        }
    }

    private void addExplosionForInvader(Invader invader) {
        if (!game.isExplosionsEnabled()) {
            return;
        }

        int centerX = invader.getX() + invader.getSize() / 2;
        int centerY = invader.getY() + invader.getSize() / 2;
        int maxRadius = Math.max(12, invader.getSize());
        game.explosions.add(new Explosion(centerX, centerY, maxRadius, EXPLOSION_DURATION_MS));
    }

    public void stopThread() {
        running = false;
    }
}
