package spaceinvaders.characters;

import spaceinvaders.UI.SpaceInvadersUI;

import java.awt.Color;

/**
 * A falling power-up collectible. The player collects it by flying into it.
 */
public class PowerUp {
    public static final int SIZE = 30;
    private static final int FALL_SPEED = 2;

    private int x, y;
    private final SpaceInvadersUI.PowerUpType type;

    public PowerUp(int x, SpaceInvadersUI.PowerUpType type) {
        this.x = x;
        this.y = -SIZE;
        this.type = type;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return SIZE; }
    public SpaceInvadersUI.PowerUpType getType() { return type; }

    public void tick() {
        y += FALL_SPEED;
    }

    public boolean isOffScreen(int screenHeight) {
        return y > screenHeight;
    }

    public String getLabel() {
        return switch (type) {
            case RAPID_FIRE  -> "FAST";
            case TRIPLE_SHOT -> "x3";
            case PIERCING    -> "PIERCE";
            case SHOTGUN     -> "BLAST";
            case LASER_BEAM  -> "LASER";
            case BOUNCING    -> "BOUNCE";
            default          -> "?";
        };
    }

    public Color getColor() {
        return switch (type) {
            case RAPID_FIRE  -> new Color(255, 80,  80);
            case TRIPLE_SHOT -> new Color(80,  200, 255);
            case PIERCING    -> new Color(200, 255, 80);
            case SHOTGUN     -> new Color(255, 165, 0);
            case LASER_BEAM  -> new Color(255, 50,  255);
            case BOUNCING    -> new Color(80,  255, 180);
            default          -> Color.WHITE;
        };
    }
}
