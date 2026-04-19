package spaceinvaders.characters;

import java.util.concurrent.ThreadLocalRandom;

public class Bullet {
    private int x, y;
    private int vx;
    private boolean piercing;
    private final long animationOffsetMs;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.animationOffsetMs = ThreadLocalRandom.current().nextLong(60_000L);
    }

    public Bullet(int x, int y, int vx) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.animationOffsetMs = ThreadLocalRandom.current().nextLong(60_000L);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getVx() { return vx; }
    public boolean isPiercing() { return piercing; }
    public long getAnimationOffsetMs() { return animationOffsetMs; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setVx(int vx) { this.vx = vx; }
    public void setPiercing(boolean piercing) { this.piercing = piercing; }
}
