package spaceinvaders.UI;


import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarsBackgroundPainter extends Thread {
    private static final int STAR_COUNT = 120;
    private final List<Point> stars = new ArrayList<>();
    private final Random random = new Random();

    private volatile boolean running = true;
    private volatile boolean started = false;
    private volatile SpaceInvadersUI game;

    public StarsBackgroundPainter() {
        setName("StarsBackgroundPainter");
        setDaemon(true);
    }

    public synchronized void attachGame(SpaceInvadersUI game) {
        this.game = game;
        initializeStarsIfNeeded();
    }

    public synchronized void startIfNeeded() {
        if (!started) {
            started = true;
            start();
        }
    }

    public synchronized List<Point> getStarsSnapshot() {
        return new ArrayList<>(stars);
    }

    public void stopPainter() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            updateStars();

            SpaceInvadersUI currentGame = game;
            if (currentGame != null) {
                currentGame.repaint();
            }

            try {
                Thread.sleep(35);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private synchronized void initializeStarsIfNeeded() {
        if (!stars.isEmpty()) {
            return;
        }

        int width = game == null ? 600 : Math.max(1, game.getWidth());
        int height = game == null ? 700 : Math.max(1, game.getHeight());

        for (int i = 0; i < STAR_COUNT; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            stars.add(new Point(x, y));
        }
    }

    private synchronized void updateStars() {
        if (stars.isEmpty()) {
            initializeStarsIfNeeded();
        }

        int width = game == null ? 600 : Math.max(1, game.getWidth());
        int height = game == null ? 700 : Math.max(1, game.getHeight());

        for (Point star : stars) {
            star.y += 1;
            if (star.y >= height) {
                star.y = 0;
                star.x = random.nextInt(width);
            }
        }
    }
}