package spaceinvaders.characters;

/**
 * Boss represents a mini-boss invader with health.
 * Spawns every ~30-50 kills as a challenge encounter.
 */
public class Boss {
    private int x, y;
    private int size;
    private int health;
    private final int maxHealth;
    private final String themePath;
    private final String shooterSkinPath;
    private final String themeName;
    private static final int BASE_SIZE = 100;
    private static final int BASE_HEALTH = 5; // Takes 5 hits to kill

    public Boss(int x, int y) {
        this(x, y, null, null, null);
    }

    public Boss(int x, int y, String themePath, String shooterSkinPath, String themeName) {
        this.x = x;
        this.y = y;
        this.size = BASE_SIZE;
        this.maxHealth = BASE_HEALTH;
        this.health = maxHealth;
        this.themePath = themePath;
        this.shooterSkinPath = shooterSkinPath;
        this.themeName = themeName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void takeDamage(int damage) {
        this.health = Math.max(0, health - damage);
    }

    public boolean isDead() {
        return health <= 0;
    }

    public float getHealthRatio() {
        return health / (float) maxHealth;
    }

    public String getThemePath() {
        return themePath;
    }

    public String getShooterSkinPath() {
        return shooterSkinPath;
    }

    public String getThemeName() {
        return themeName;
    }
}
