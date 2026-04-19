package spaceinvaders.characters;

public class Invader {
    private int x, y, size;
    private final boolean rickRollTarget;

    public Invader(int x, int y, int size) {
        this(x, y, size, false);
    }

    public Invader(int x, int y, int size, boolean rickRollTarget) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.rickRollTarget = rickRollTarget;
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
}
