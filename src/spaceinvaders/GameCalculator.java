package spaceinvaders;

import spaceinvaders.UI.SpaceInvadersUI;
import spaceinvaders.characters.Bullet;
import spaceinvaders.characters.DeathEffect;
import spaceinvaders.characters.Explosion;
import spaceinvaders.characters.Invader;
import spaceinvaders.characters.PowerUp;

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
    private static final int RICK_INVADER_CHANCE_PERCENT = 2;
        private static final long MODIFIER_DURATION_MS = 8000;
    private static final long POWER_UP_DURATION_MS = 8000;
    private static final long LASER_BEAM_FLASH_MS = 120;
    private long lastFireTimeMs = 0;
    private long nextModifierRollMs = System.currentTimeMillis() + 15000;
    private long nextAchievementMs = System.currentTimeMillis() + 12000;
    private long nextPowerUpSpawnMs = System.currentTimeMillis() + 18000;
    private int spawnCounter = 0;

        private static final String[] BOSS_ROASTS = {
            "Boss Roast: You fight like a loading bar",
            "Boss Roast: Your aim has trust issues",
            "Boss Roast: Even invaders feel bad for you",
            "Boss Roast: Who taught you dodging, a potato?"
        };

        private static final String[] FAKE_ACHIEVEMENTS = {
            "Achievement Unlocked: Professional Button Presser",
            "Achievement Unlocked: Certified Space Janitor",
            "Achievement Unlocked: Panic Strategist",
            "Achievement Unlocked: Physics? Optional"
        };

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
                    GameExceptions.handleInterrupted("GameCalculator loop", e);
                    break;
                }
            }
        }
    }

    private void updateGameState() {
        game.updateTemporaryRickThemeRestore();

        // Skip game updates if paused
        if (game.isPaused()) {
            return;
        }
        updateSillyModeState();

        updateShooterPosition();
        handleShooting();
        updateInvaderPositions();
        updateBulletPositions();
        checkCollisions();
        updateExplosions();
        updateDeathEffects();
        spawnNewInvaders();
        spawnPowerUp();
        updatePowerUpPositions();
        checkPowerUpCollection();
    }

    private void updateSillyModeState() {
        long now = System.currentTimeMillis();

        if (!game.isSillinessModeEnabled()) {
            return;
        }

        if (game.isModifierExpired()) {
            game.clearActiveSillyModifier();
            game.setAnnouncerMessage("Modifier ended. Back to normal chaos.", 1500);
        }

        if (now >= nextModifierRollMs && game.getActiveSillyModifier() == SpaceInvadersUI.SillyModifier.NONE) {
            activateRandomModifier();
            nextModifierRollMs = now + 20000 + game.random.nextInt(20000);
        }

        if (now >= nextAchievementMs) {
            String achievement = FAKE_ACHIEVEMENTS[game.random.nextInt(FAKE_ACHIEVEMENTS.length)];
            game.setFakeAchievementMessage(achievement, 2400);
            nextAchievementMs = now + 16000 + game.random.nextInt(9000);
        }
    }

    private void activateRandomModifier() {
        SpaceInvadersUI.SillyModifier[] modifiers = {
                SpaceInvadersUI.SillyModifier.MOON_GRAVITY,
                SpaceInvadersUI.SillyModifier.ZOOMIES,
                SpaceInvadersUI.SillyModifier.TINY_PANIC,
                SpaceInvadersUI.SillyModifier.MIRROR,
                SpaceInvadersUI.SillyModifier.DISCO,
                SpaceInvadersUI.SillyModifier.PACIFIST
        };

        SpaceInvadersUI.SillyModifier selected = modifiers[game.random.nextInt(modifiers.length)];
        String roast = BOSS_ROASTS[game.random.nextInt(BOSS_ROASTS.length)];
        String banner = getModifierBanner(selected) + " | " + roast;
        game.activateSillyModifier(selected, MODIFIER_DURATION_MS, banner);
    }

    private String getModifierBanner(SpaceInvadersUI.SillyModifier modifier) {
        switch (modifier) {
        case MOON_GRAVITY:
            return "Moon Gravity Mode!";
        case ZOOMIES:
            return "Zoomies Mode!";
        case TINY_PANIC:
            return "Tiny Panic Mode!";
        case MIRROR:
            return "Mirror Controls Mode!";
        case DISCO:
            return "Disco Mode!";
        case PACIFIST:
            return "Weapon Jam Mode!";
        default:
            return "Silly Mode!";
        }
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

    private void updateDeathEffects() {
        synchronized (game) {
            Iterator<DeathEffect> it = game.deathEffects.iterator();
            while (it.hasNext()) {
                if (it.next().isExpired()) {
                    it.remove();
                }
            }
        }
    }

    private void handleShooting() {
        synchronized (game) {
            if (game.isGameOver() || !game.fireHeld) {
                return;
            }

            long now = System.currentTimeMillis();
            SpaceInvadersUI.PowerUpType powerUp = game.getActivePowerUp();
            long interval = powerUp == SpaceInvadersUI.PowerUpType.RAPID_FIRE ? FIRE_INTERVAL_MS / 3 : FIRE_INTERVAL_MS;
            if (now - lastFireTimeMs < interval) {
                return;
            }

            if (game.isPacifistModeActive() && game.random.nextInt(100) < 45) {
                int centerX = game.getShooter_X_Coordinate() + game.getShooterWidth() / 2;
                int centerY = game.getHeight() - game.getShooterHeight();
                game.explosions.add(new Explosion(centerX, centerY, 10, 140));
                game.setAnnouncerMessage("JAMMED!", 500);
                lastFireTimeMs = now;
                return;
            }

            int shooterX = game.getShooter_X_Coordinate();
            int shooterWidth = game.getShooterWidth();
            int shooterHeight = game.getShooterHeight();
            int baseX = shooterX + shooterWidth / 2;
            int baseY = game.getHeight() - shooterHeight;

            switch (powerUp) {
                case TRIPLE_SHOT -> {
                    game.bullets.add(new Bullet(baseX, baseY, 0));
                    game.bullets.add(new Bullet(baseX - 18, baseY, -1));
                    game.bullets.add(new Bullet(baseX + 18, baseY, 1));
                }
                case SHOTGUN -> {
                    for (int i = -2; i <= 2; i++) {
                        game.bullets.add(new Bullet(baseX + i * 14, baseY, i * 2));
                    }
                }
                case BOUNCING -> {
                    Bullet b = new Bullet(baseX, baseY, 0);
                    b.setBouncing(true);
                    game.bullets.add(b);
                }
                case PIERCING -> {
                    Bullet pb = new Bullet(baseX, baseY, 0);
                    pb.setPiercing(true);
                    game.bullets.add(pb);
                }
                case LASER_BEAM -> fireLaserBeam(shooterX, shooterWidth, baseX, now);
                default -> game.bullets.add(new Bullet(baseX, baseY, 0));
            }
            lastFireTimeMs = now;
        }
    }

    private void fireLaserBeam(int shooterX, int shooterWidth, int beamX, long now) {
        game.laserBeamX = beamX;
        game.laserBeamUntilMs = now + LASER_BEAM_FLASH_MS;
        int beamLeft  = beamX - 12;
        int beamRight = beamX + 12;
        Iterator<Invader> it = game.invaders.iterator();
        while (it.hasNext()) {
            Invader inv = it.next();
            int cx = inv.getX() + inv.getSize() / 2;
            if (cx >= beamLeft && cx <= beamRight) {
                if (inv.isRickRollTarget()) game.handleRickRollKill();
                addExplosionForInvader(inv);
                it.remove();
                game.recordInvaderDefeatCombo();
                int points = game.getActiveSillyModifier() == SpaceInvadersUI.SillyModifier.TINY_PANIC ? 20 : 10;
                game.addPoints(points);
            }
        }
    }

    private void updateShooterPosition() {
        synchronized (game) {
            int shooter_X_Coordinate = game.getShooter_X_Coordinate();
            int shooter_Width = game.getShooterWidth();
            int movementStep = game.getActiveSillyModifier() == SpaceInvadersUI.SillyModifier.ZOOMIES ? 9 : 5;
            
            // Move shooter left or right
            if (game.moveLeft && shooter_X_Coordinate > 0) {
                game.setShooter_X_Coordinate(shooter_X_Coordinate - movementStep);
            }
            if (game.moveRight && shooter_X_Coordinate < game.getWidth() - shooter_Width) {
                game.setShooter_X_Coordinate(shooter_X_Coordinate + movementStep);
            }
        }
    }

    private void spawnNewInvaders() {
        synchronized (game) {
            if (game.random.nextInt(100) < 5) {
                int x = game.random.nextInt(game.getWidth());
                boolean isRickInvader = game.random.nextInt(100) < RICK_INVADER_CHANCE_PERCENT;
                spawnCounter++;

                int baseSize = 40;
                if (game.getActiveSillyModifier() == SpaceInvadersUI.SillyModifier.TINY_PANIC) {
                    baseSize = 20;
                }

                // Big-head gag: every 7th invader is oversized.
                if (game.isSillinessModeEnabled() && spawnCounter % 7 == 0) {
                    baseSize = Math.max(baseSize, 72);
                }

                game.invaders.add(new Invader(x, 0, baseSize, isRickInvader));
            }
        }
    }

    private void updateInvaderPositions() {
        synchronized (game) {
            Iterator<Invader> invaderIterator = game.invaders.iterator();
            while (invaderIterator.hasNext()) {
                Invader invader = invaderIterator.next();
                int y = invader.getY();
                int step = 2;
                if (game.getActiveSillyModifier() == SpaceInvadersUI.SillyModifier.MOON_GRAVITY) {
                    step = 1;
                } else if (game.getActiveSillyModifier() == SpaceInvadersUI.SillyModifier.ZOOMIES) {
                    step = 4;
                }

                invader.setY(y + step);
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
                int yStep = 5;
                if (game.getActiveSillyModifier() == SpaceInvadersUI.SillyModifier.MOON_GRAVITY) {
                    yStep = 3;
                    int drift = (int) Math.signum(Math.sin((bullet.getY() + bullet.getX()) * 0.07));
                    bullet.setX(Math.max(0, Math.min(game.getWidth(), bullet.getX() + drift)));
                } else if (game.getActiveSillyModifier() == SpaceInvadersUI.SillyModifier.ZOOMIES) {
                    yStep = 8;
                }

                // Apply horizontal velocity (spread shots)
                if (bullet.getVx() != 0) {
                    int newX = bullet.getX() + bullet.getVx();
                    if (bullet.isBouncing()) {
                        if (newX < 0 || newX > game.getWidth()) {
                            bullet.setVx(-bullet.getVx());
                            newX = bullet.getX() + bullet.getVx();
                        }
                    }
                    bullet.setX(newX);
                }

                bullet.setY(y - yStep);
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
                        if (invader.isRickRollTarget()) {
                            game.handleRickRollKill();
                        }
                        addExplosionForInvader(invader);
                        if (!bullet.isPiercing()) {
                            bulletIterator.remove();
                        }
                        invaderIterator.remove();
                        game.recordInvaderDefeatCombo();
                        int points = game.getActiveSillyModifier() == SpaceInvadersUI.SillyModifier.TINY_PANIC ? 20 : 10;
                        game.addPoints(points);
                        if (!bullet.isPiercing()) break;
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
        int centerX = invader.getX() + invader.getSize() / 2;
        int centerY = invader.getY() + invader.getSize() / 2;

        String deathExplosionSound = game.getDeathExplosionSoundEffectPath();
        if (deathExplosionSound != null && game.getMusicHandler() != null) {
            game.getMusicHandler().playOneShotEffect(deathExplosionSound);
        }

        // If a death skin is set, show the flash/fade effect instead of the normal explosion
        java.awt.Image deathSkin = game.imageSelection.getDeathSkinImage();
        if (deathSkin != null) {
            synchronized (game) {
                game.deathEffects.add(new DeathEffect(invader.getX(), invader.getY(), invader.getSize(), deathSkin,
                        game.imageSelection.isDeathSkinFadeOutEnabled()));
            }
            return;
        }

        if (!game.isExplosionsEnabled()) {
            return;
        }

        int maxRadius = Math.max(12, invader.getSize());
        synchronized (game) {
            game.explosions.add(new Explosion(centerX, centerY, maxRadius, EXPLOSION_DURATION_MS));
        }
    }

    public void stopThread() {
        running = false;
    }

    // ----------------------------- Power-up lifecycle ---------------------------

    private void spawnPowerUp() {
        long now = System.currentTimeMillis();
        if (now < nextPowerUpSpawnMs) return;
        SpaceInvadersUI.PowerUpType[] types = {
            SpaceInvadersUI.PowerUpType.RAPID_FIRE,
            SpaceInvadersUI.PowerUpType.TRIPLE_SHOT,
            SpaceInvadersUI.PowerUpType.PIERCING,
            SpaceInvadersUI.PowerUpType.SHOTGUN,
            SpaceInvadersUI.PowerUpType.LASER_BEAM,
            SpaceInvadersUI.PowerUpType.BOUNCING
        };
        SpaceInvadersUI.PowerUpType type = types[game.random.nextInt(types.length)];
        int x = game.random.nextInt(Math.max(1, game.getWidth() - PowerUp.SIZE));
        synchronized (game) {
            game.powerUps.add(new PowerUp(x, type));
        }
        nextPowerUpSpawnMs = now + 15000 + game.random.nextInt(10000);
    }

    private void updatePowerUpPositions() {
        synchronized (game) {
            Iterator<PowerUp> it = game.powerUps.iterator();
            while (it.hasNext()) {
                PowerUp p = it.next();
                p.tick();
                if (p.isOffScreen(game.getHeight())) it.remove();
            }
        }
    }

    private void checkPowerUpCollection() {
        int sx = game.getShooter_X_Coordinate();
        int sy = game.getHeight() - game.getShooterHeight();
        Rectangle shooter = new Rectangle(sx, sy, game.getShooterWidth(), game.getShooterHeight());
        synchronized (game) {
            Iterator<PowerUp> it = game.powerUps.iterator();
            while (it.hasNext()) {
                PowerUp p = it.next();
                if (shooter.intersects(new Rectangle(p.getX(), p.getY(), PowerUp.SIZE, PowerUp.SIZE))) {
                    game.activatePowerUp(p.getType(), POWER_UP_DURATION_MS);
                    it.remove();
                }
            }
        }
    }
}
