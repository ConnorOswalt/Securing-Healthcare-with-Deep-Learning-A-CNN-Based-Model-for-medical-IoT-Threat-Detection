package spaceinvaders.characters;

public class Explosion {
    private final int centerX;
    private final int centerY;
    private final int maxRadius;
    private final long createdAtMs;
    private final long durationMs;

    public Explosion(int centerX, int centerY, int maxRadius, long durationMs) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.maxRadius = maxRadius;
        this.durationMs = durationMs;
        this.createdAtMs = System.currentTimeMillis();
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getMaxRadius() {
        return maxRadius;
    }

    public double getProgress() {
        long elapsed = System.currentTimeMillis() - createdAtMs;
        if (durationMs <= 0) {
            return 1.0;
        }
        double progress = elapsed / (double) durationMs;
        if (progress < 0) {
            return 0;
        }
        if (progress > 1) {
            return 1;
        }
        return progress;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAtMs >= durationMs;
    }
}
