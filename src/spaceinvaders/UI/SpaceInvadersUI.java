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
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class SpaceInvadersUI extends JPanel implements KeyListener {
    public enum SillyModifier {
        NONE,
        MOON_GRAVITY,
        ZOOMIES,
        TINY_PANIC,
        MIRROR,
        DISCO,
        PACIFIST
    }

    public enum PowerUpType {
        NONE,
        RAPID_FIRE,
        TRIPLE_SHOT,
        PIERCING,
        SHOTGUN,
        LASER_BEAM,
        BOUNCING
    }

    private static SpaceInvadersUI activeInstance;
    private static final String DEATH_SOUND_EFFECT_PATH = "/resources/SoundEffects/player_death.wav";
    private static final String RICK_THEME_PATH = "/resources/Themes/Rick.json";
    private static final String RICK_ROLL_MUSIC_PATH = "/resources/Music/NeverGonnaGiveYouUp.wav";
    private static final String RICK_ROLL_MUSIC_FALLBACK_PATH = "/resources/Music/Retro.wav";
    private static final String RICK_ROLL_BACKGROUND_PATH = "/resources/Background/RickAstleyDance.gif";
    private static final String RICK_ROLL_BACKGROUND_FALLBACK_PATH = "/resources/Background/peterWriting.gif";
    private static final long MIN_TEMPORARY_RICK_THEME_DURATION_MS = 3000;
    private static final long TEMPORARY_RICK_THEME_DURATION_RANGE_MS = 5000;
    private static final long MINIMUM_RICK_SNIPPET_REMAINING_MS = 8000;

    private final Timer repaintTimer;
    public ArrayList<Invader> invaders;
    public ArrayList<Bullet> bullets;
    public ArrayList<Explosion> explosions;
    public ArrayList<DeathEffect> deathEffects;
    public List<spaceinvaders.characters.PowerUp> powerUps;
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
    private static final int MAX_PLAYER_HEALTH = 100;
    private static final int PLAYER_HIT_DAMAGE = 34;
    private static final int INVADER_KILL_HEAL = 6;
    private int playerHealth = MAX_PLAYER_HEALTH;
    private boolean playerFlashing = false;
    private long playerFlashStartTime = 0;
    private static final long PLAYER_FLASH_DURATION = 300;
    private boolean gameOver = false;
    private boolean deathSoundPlayed = false;
    private boolean deathSoundEnabled = true;
    private boolean deathSoundLooping = false;
    private String deathSoundEffectPath = DEATH_SOUND_EFFECT_PATH;
    private String deathExplosionSoundEffectPath;
    private long gameOverFlashStartTime = 0;
    private static final long GAME_OVER_FLASH_DURATION = 500; // Flash for 500ms after game over
    private boolean paused = false;
    private boolean sillinessModeEnabled = true;
    private SillyModifier activeSillyModifier = SillyModifier.NONE;
    private long activeModifierUntilMs = 0;
    private PowerUpType activePowerUp = PowerUpType.NONE;
    private long activePowerUpUntilMs = 0;
    public int laserBeamX = -1;
    public long laserBeamUntilMs = 0;
    private String announcerMessage = "";
    private long announcerMessageUntilMs = 0;
    private String fakeAchievementMessage = "";
    private long fakeAchievementUntilMs = 0;
    private String comboMessage = "";
    private long comboMessageUntilMs = 0;
    private int comboCount = 0;
    private long comboWindowUntilMs = 0;
    private String currentThemePath;
    private String temporaryRickRestoreThemePath;
    private long temporaryRickRestoreAtMs = 0;
    private boolean temporaryRickRestoreToDefaultState = false;
    private boolean pendingRandomRickSnippet = false;
    private boolean pendingResumeInterruptedTrackAfterRick = false;

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
        powerUps = new ArrayList<>();
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

        // Draw power-up collectibles
        paintingActions.drawPowerUps(g, this);

        // Draw bullets
        paintingActions.drawBullets(g, this);

        // Laser beam flash
        paintingActions.drawLaserBeam(g, this);

        // Draw player health hearts
        paintingActions.drawPlayerHealth(g, this);

        // Draw current score
        paintingActions.drawCurrentScore(g, this);

        // Active power-up HUD bar
        paintingActions.drawActivePowerUpHud(g, this);

        drawSillyOverlay(g);
    }

    private void drawSillyOverlay(Graphics g) {
        long now = System.currentTimeMillis();
        Graphics2D g2d = (Graphics2D) g;
        Font originalFont = g2d.getFont();
        AffineTransform originalTransform = g2d.getTransform();

        if (sillinessModeEnabled && activeSillyModifier == SillyModifier.DISCO && now < activeModifierUntilMs) {
            float phase = (now % 1200L) / 1200.0f;
            Color disco = Color.getHSBColor(phase, 0.7f, 1.0f);
            g2d.setColor(new Color(disco.getRed(), disco.getGreen(), disco.getBlue(), 45));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();

        if (!announcerMessage.isEmpty() && now < announcerMessageUntilMs) {
            int w = fm.stringWidth(announcerMessage);
            int x = (getWidth() - w) / 2;
            int y = Math.max(84, getHeight() / 3);
            g2d.setColor(new Color(0, 0, 0, 170));
            g2d.fillRoundRect(x - 18, y - 28, w + 36, 40, 16, 16);
            g2d.setColor(new Color(255, 255, 255, 35));
            g2d.drawRoundRect(x - 18, y - 28, w + 36, 40, 16, 16);
            g2d.setColor(Color.WHITE);
            g2d.drawString(announcerMessage, x, y);
        }

        if (sillinessModeEnabled && activeSillyModifier != SillyModifier.NONE && now < activeModifierUntilMs) {
            String effectName = getActiveEffectDisplayName();
            long remainingMs = Math.max(0, activeModifierUntilMs - now);
            float remainingRatio = Math.min(1.0f, remainingMs / 8000.0f);
            int panelWidth = 220;
            int panelHeight = 58;
            int panelX = getWidth() - panelWidth - 18;
            int panelY = 34;

            g2d.setColor(new Color(0, 0, 0, 170));
            g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 16, 16);
            g2d.setColor(new Color(255, 255, 255, 40));
            g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 16, 16);

            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.setColor(new Color(180, 220, 255));
            g2d.drawString("CURRENT EFFECT", panelX + 12, panelY + 18);

            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.setColor(Color.WHITE);
            g2d.drawString(effectName, panelX + 12, panelY + 38);

            g2d.setColor(new Color(45, 45, 45, 210));
            g2d.fillRoundRect(panelX + 12, panelY + 43, panelWidth - 24, 8, 8, 8);
            g2d.setColor(new Color(255, 120, 60));
            g2d.fillRoundRect(panelX + 12, panelY + 43, (int) ((panelWidth - 24) * remainingRatio), 8, 8, 8);
        }

        if (!fakeAchievementMessage.isEmpty() && now < fakeAchievementUntilMs) {
            g2d.setFont(new Font("Arial", Font.BOLD, 22));
            fm = g2d.getFontMetrics();
            int w = fm.stringWidth(fakeAchievementMessage);
            int x = (getWidth() - w) / 2;
            int y = getHeight() - 28;
            g2d.setColor(new Color(10, 10, 10, 190));
            g2d.fillRoundRect(x - 12, y - 22, w + 24, 30, 14, 14);
            g2d.setColor(new Color(90, 255, 150));
            g2d.drawString(fakeAchievementMessage, x, y);
        }

        if (!comboMessage.isEmpty() && now < comboMessageUntilMs) {
            float pulse = 1.0f + 0.18f * (float) Math.sin(now * 0.018);
            Font comboFont = new Font("Arial", Font.BOLD, 24);
            g2d.setFont(comboFont);
            FontMetrics comboFm = g2d.getFontMetrics();
            int w = comboFm.stringWidth(comboMessage);
            int x = getWidth() - w - 18;
            int y = (sillinessModeEnabled && activeSillyModifier != SillyModifier.NONE && now < activeModifierUntilMs)
                    ? 136
                    : 96;
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRoundRect(x - 16, y - 30, w + 32, 42, 14, 14);
            g2d.translate(x + w / 2.0, y - 10.0);
            g2d.scale(pulse, pulse);
            g2d.setColor(new Color(255, 220, 90));
            g2d.drawString(comboMessage, -comboFm.stringWidth(comboMessage) / 2, comboFm.getAscent() / 2);
            g2d.setTransform(originalTransform);
        }

        g2d.setFont(originalFont);
    }

    private String getActiveEffectDisplayName() {
        return switch (activeSillyModifier) {
            case MOON_GRAVITY -> "MOON GRAVITY";
            case ZOOMIES -> "ZOOMIES";
            case TINY_PANIC -> "TINY PANIC";
            case MIRROR -> "MIRROR";
            case DISCO -> "DISCO";
            case PACIFIST -> "WEAPON JAM";
            default -> "NONE";
        };
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

    public void setCurrentThemePath(String currentThemePath) {
        this.currentThemePath = currentThemePath;
    }

    public boolean consumePendingRandomRickSnippet() {
        boolean shouldUseRandomSnippet = pendingRandomRickSnippet;
        pendingRandomRickSnippet = false;
        return shouldUseRandomSnippet;
    }

    public boolean consumePendingResumeInterruptedTrackAfterRick() {
        boolean shouldResumeInterruptedTrack = pendingResumeInterruptedTrackAfterRick;
        pendingResumeInterruptedTrackAfterRick = false;
        return shouldResumeInterruptedTrack;
    }

    public long getMinimumRickSnippetRemainingMs() {
        return MINIMUM_RICK_SNIPPET_REMAINING_MS;
    }

    public void clearTemporaryRickRestore() {
        temporaryRickRestoreThemePath = null;
        temporaryRickRestoreAtMs = 0;
        temporaryRickRestoreToDefaultState = false;
    }

    public void updateTemporaryRickThemeRestore() {
        if ((!temporaryRickRestoreToDefaultState && temporaryRickRestoreThemePath == null)
                || System.currentTimeMillis() < temporaryRickRestoreAtMs) {
            return;
        }

        boolean restoreDefaultState = temporaryRickRestoreToDefaultState;
        String restoreThemePath = temporaryRickRestoreThemePath;
        clearTemporaryRickRestore();
        if (restoreDefaultState) {
            imageSelection.restoreDefaultThemeState(this);
            currentThemePath = null;
            setDeathSoundEnabled(true);
            setDeathSoundLooping(false);
            setDeathSoundEffectPath(DEATH_SOUND_EFFECT_PATH);
            clearDeathExplosionSoundEffectPath();
            if (musicHandler != null) {
                musicHandler.resumeInterruptedTrack();
            }
            repaint();
            return;
        }

        ThemeImplementation.requestThemeChange(this, restoreThemePath);
    }

    public boolean isSillinessModeEnabled() {
        return sillinessModeEnabled;
    }

    public void setSillinessModeEnabled(boolean sillinessModeEnabled) {
        this.sillinessModeEnabled = sillinessModeEnabled;
        if (!sillinessModeEnabled) {
            clearActiveSillyModifier();
        }
    }

    public SillyModifier getActiveSillyModifier() {
        return activeSillyModifier;
    }

    public boolean isMirrorControlsActive() {
        return sillinessModeEnabled && activeSillyModifier == SillyModifier.MIRROR
                && System.currentTimeMillis() < activeModifierUntilMs;
    }

    public boolean isPacifistModeActive() {
        return sillinessModeEnabled && activeSillyModifier == SillyModifier.PACIFIST
                && System.currentTimeMillis() < activeModifierUntilMs;
    }

    public void activateSillyModifier(SillyModifier modifier, long durationMs, String bannerText) {
        this.activeSillyModifier = modifier;
        this.activeModifierUntilMs = System.currentTimeMillis() + durationMs;
        setAnnouncerMessage(bannerText, 2600);
    }

    public void clearActiveSillyModifier() {
        this.activeSillyModifier = SillyModifier.NONE;
        this.activeModifierUntilMs = 0;
    }

    public PowerUpType getActivePowerUp() {
        if (activePowerUp != PowerUpType.NONE && System.currentTimeMillis() >= activePowerUpUntilMs) {
            activePowerUp = PowerUpType.NONE;
        }
        return activePowerUp;
    }

    public long getActivePowerUpUntilMs() {
        return activePowerUpUntilMs;
    }

    public void activatePowerUp(PowerUpType type, long durationMs) {
        this.activePowerUp = type;
        this.activePowerUpUntilMs = System.currentTimeMillis() + durationMs;
        String name = switch (type) {
            case RAPID_FIRE  -> "RAPID FIRE!";
            case TRIPLE_SHOT -> "TRIPLE SHOT!";
            case PIERCING    -> "PIERCING ROUNDS!";
            case SHOTGUN     -> "SHOTGUN BLAST!";
            case LASER_BEAM  -> "LASER BEAM!";
            case BOUNCING    -> "BOUNCING BULLETS!";
            default          -> "";
        };
        if (!name.isEmpty()) setAnnouncerMessage(name, 2000);
    }

    public void clearActivePowerUp() {
        this.activePowerUp = PowerUpType.NONE;
        this.activePowerUpUntilMs = 0;
    }

    public boolean isModifierExpired() {
        return activeSillyModifier != SillyModifier.NONE && System.currentTimeMillis() >= activeModifierUntilMs;
    }

    public void setAnnouncerMessage(String message, long durationMs) {
        this.announcerMessage = message == null ? "" : message;
        this.announcerMessageUntilMs = System.currentTimeMillis() + durationMs;
    }

    public void setFakeAchievementMessage(String message, long durationMs) {
        this.fakeAchievementMessage = message == null ? "" : message;
        this.fakeAchievementUntilMs = System.currentTimeMillis() + durationMs;
    }

    public void recordInvaderDefeatCombo() {
        long now = System.currentTimeMillis();
        if (now > comboWindowUntilMs) {
            comboCount = 0;
        }
        comboCount++;
        comboWindowUntilMs = now + 1800;

        if (comboCount >= 2) {
            comboMessage = getComboTitle(comboCount) + " x" + comboCount;
            comboMessageUntilMs = now + 1400;
        }
    }

    private String getComboTitle(int comboCount) {
        if (comboCount >= 8) {
            return "Cosmic Bonk";
        }
        if (comboCount >= 5) {
            return "Mega Bonk";
        }
        return "Double Bonk";
    }

    public void setDeathSoundEffectPath(String deathSoundEffectPath) {
        if (deathSoundEffectPath == null || deathSoundEffectPath.isBlank()) {
            this.deathSoundEffectPath = DEATH_SOUND_EFFECT_PATH;
            return;
        }
        this.deathSoundEffectPath = deathSoundEffectPath;
    }

    public void setDeathSoundEnabled(boolean deathSoundEnabled) {
        this.deathSoundEnabled = deathSoundEnabled;
    }

    public void setDeathSoundLooping(boolean deathSoundLooping) {
        this.deathSoundLooping = deathSoundLooping;
    }

    public String getDefaultDeathSoundEffectPath() {
        return DEATH_SOUND_EFFECT_PATH;
    }

    public void setDeathExplosionSoundEffectPath(String deathExplosionSoundEffectPath) {
        this.deathExplosionSoundEffectPath = deathExplosionSoundEffectPath;
    }

    public void clearDeathExplosionSoundEffectPath() {
        this.deathExplosionSoundEffectPath = null;
    }

    public String getDeathExplosionSoundEffectPath() {
        return deathExplosionSoundEffectPath;
    }

    public void handleRickRollKill() {
        if (RICK_THEME_PATH.equals(currentThemePath)) {
            return;
        }

        if (SpaceInvadersUI.class.getResource(RICK_THEME_PATH) != null) {
            long randomRickDurationMs = MIN_TEMPORARY_RICK_THEME_DURATION_MS
                    + random.nextInt((int) TEMPORARY_RICK_THEME_DURATION_RANGE_MS + 1);
            if (currentThemePath != null && !currentThemePath.isBlank()) {
                temporaryRickRestoreThemePath = currentThemePath;
                temporaryRickRestoreAtMs = System.currentTimeMillis() + randomRickDurationMs;
            } else {
                temporaryRickRestoreThemePath = null;
                temporaryRickRestoreToDefaultState = true;
                temporaryRickRestoreAtMs = System.currentTimeMillis() + randomRickDurationMs;
            }
            pendingRandomRickSnippet = true;
            pendingResumeInterruptedTrackAfterRick = true;
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

    public int getMaxPlayerHealth() {
        return MAX_PLAYER_HEALTH;
    }

    public void healPlayerFromInvaderKill() {
        if (gameOver || playerHealth <= 0) {
            return;
        }

        playerHealth = Math.min(MAX_PLAYER_HEALTH, playerHealth + INVADER_KILL_HEAL);
    }

    public void damagePlayer() {
        if (playerHealth <= 0 || gameOver) {
            return;
        }

        playerHealth = Math.max(0, playerHealth - PLAYER_HIT_DAMAGE);
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
                if (!deathSoundPlayed && deathSoundEnabled) {
                    if (deathSoundLooping) {
                        long gifDurationMs = imageSelection.getDeathScreenGifDurationMs();
                        if (gifDurationMs > 0) {
                            musicHandler.playLoopingEffectSyncedTo(deathSoundEffectPath, gifDurationMs);
                        } else {
                            musicHandler.playLoopingEffect(deathSoundEffectPath);
                        }
                    } else {
                        musicHandler.playOneShotEffect(deathSoundEffectPath);
                    }
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
            powerUps.clear();
            clearActivePowerUp();
            laserBeamX = -1;
            laserBeamUntilMs = 0;
            gameOver = false;
            deathSoundPlayed = false;
            gameOverFlashStartTime = 0; // Reset game over flash timer
            if (musicHandler != null) {
                musicHandler.stopLoopingEffect();
            }
            playerHealth = MAX_PLAYER_HEALTH;
            playerFlashing = false;
            shooter_X_Coordinate = 200;
            moveLeft = false;
            moveRight = false;
            fireHeld = false;
            clearActiveSillyModifier();
            announcerMessage = "";
            fakeAchievementMessage = "";
            comboMessage = "";
            comboCount = 0;
            comboWindowUntilMs = 0;
            clearTemporaryRickRestore();
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
