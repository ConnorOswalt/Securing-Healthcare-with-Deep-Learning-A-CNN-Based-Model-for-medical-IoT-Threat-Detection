package spaceinvaders.characters;

public class Invader {
    private int x, y, size;
    private final boolean rickRollTarget;
    private int speedPixelsPerUpdate;

    public Invader(int x, int y, int size) {
        this(x, y, size, false, 2);
    }

    public Invader(int x, int y, int size, boolean rickRollTarget) {
        this(x, y, size, rickRollTarget, 2);
    }

    public Invader(int x, int y, int size, boolean rickRollTarget, int speed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.rickRollTarget = rickRollTarget;
        this.speedPixelsPerUpdate = speed;
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

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isRickRollTarget() {
        return rickRollTarget;
    }

    public int getSpeed() {
        return speedPixelsPerUpdate;
    }

    public void setSpeed(int speed) {
        this.speedPixelsPerUpdate = speed;
    }
}
