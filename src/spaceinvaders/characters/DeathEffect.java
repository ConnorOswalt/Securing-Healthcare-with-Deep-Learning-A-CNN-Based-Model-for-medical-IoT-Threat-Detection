package spaceinvaders.characters;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

/**
 * Represents a death-skin effect played at the location of a killed invader.
 * The image fades out smoothly over its lifetime.
 */
public class DeathEffect {
    private static final long DURATION_MS = 500;

    private final int x, y, size;
    private final Image deathSkinImage;
    private final boolean fadeOut;
    private final long createdAtMs;
    private BufferedImage cachedFrame;

    public DeathEffect(int x, int y, int size, Image deathSkinImage, boolean fadeOut) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.deathSkinImage = deathSkinImage;
        this.fadeOut = fadeOut;
        this.createdAtMs = System.currentTimeMillis();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return size; }
    public Image getDeathSkinImage() { return deathSkinImage; }

    /**
     * Returns a cached BufferedImage of this death skin at its size.
     * The image is the same for every frame, so it is rendered once and reused.
     */
    public BufferedImage getCachedFrame(ImageObserver observer) {
        if (cachedFrame == null) {
            cachedFrame = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = cachedFrame.createGraphics();
            g.drawImage(deathSkinImage, 0, 0, size, size, observer);
            g.dispose();
        }
        return cachedFrame;
    }

    public double getProgress() {
        long elapsed = System.currentTimeMillis() - createdAtMs;
        return Math.min(1.0, elapsed / (double) DURATION_MS);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAtMs >= DURATION_MS;
    }

    /** Returns alpha (0–255) to use when drawing. */
    public int getAlpha() {
        if (!fadeOut) {
            return 255;
        }

        double progress = getProgress();
        return (int) (255 * (1.0 - progress));
    }
}
