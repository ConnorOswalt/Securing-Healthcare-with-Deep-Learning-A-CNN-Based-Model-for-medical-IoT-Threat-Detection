package spaceinvaders.characters;

public class Bullet {
    private int x, y;
    private int vx;
    private boolean piercing;
    private boolean bouncing;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
    }

    public Bullet(int x, int y, int vx) {
        this.x = x;
        this.y = y;
        this.vx = vx;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getVx() { return vx; }
    public boolean isPiercing() { return piercing; }
    public boolean isBouncing() { return bouncing; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setVx(int vx) { this.vx = vx; }
    public void setPiercing(boolean piercing) { this.piercing = piercing; }
    public void setBouncing(boolean bouncing) { this.bouncing = bouncing; }
}
