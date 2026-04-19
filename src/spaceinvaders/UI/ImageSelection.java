package spaceinvaders.UI;

import spaceinvaders.GameExceptions;

import java.awt.*;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;

public class ImageSelection {
    private static final String RICK_INVADER_IMAGE_PATH = "/resources/Shooter/Rick.png";
    private Image shooterImage;
    private Image invaderImage;
    private Image rickInvaderImage;
    private Image bulletImage;
    private Image backgroundImage;
    private Image deathSkinImage;
    private Image deathScreenImage;
    private boolean deathSkinFadeOut = true;
    private boolean starsBackgroundEnabled;
    private final StarsBackgroundPainter starsBackgroundPainter = new StarsBackgroundPainter();

    public Image getShooterImage() {
        return shooterImage;
    }

    public Image getInvaderImage() {
        return invaderImage;
    }

    public Image getRickInvaderImage() {
        if (rickInvaderImage == null) {
            rickInvaderImage = loadImageIfPresent(RICK_INVADER_IMAGE_PATH);
        }
        return rickInvaderImage;
    }

    public Image getBulletImage() {
        return bulletImage;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public Image getDeathSkinImage() {
        return deathSkinImage;
    }

    public Image getDeathScreenImage() {
        return deathScreenImage;
    }

    public boolean isDeathSkinFadeOutEnabled() {
        return deathSkinFadeOut;
    }

    public void setDeathSkinImageFromResourcePath(String resourcePath) {
        deathSkinImage = loadImageIfPresent(resourcePath);
    }

    public void setDeathSkinFadeOut(boolean deathSkinFadeOut) {
        this.deathSkinFadeOut = deathSkinFadeOut;
    }

    public void clearDeathSkinImage() {
        deathSkinImage = null;
        deathSkinFadeOut = true;
    }

    public void setDeathScreenImageFromResourcePath(String resourcePath) {
        Image loadedImage = loadImage("death screen", resourcePath);
        if (loadedImage != null) {
            deathScreenImage = loadedImage;
        }
    }

    public void clearDeathScreenImage() {
        deathScreenImage = null;
    }

    public boolean isStarsBackgroundEnabled() {
        return starsBackgroundEnabled;
    }

    public List<Point> getStarsSnapshot() {
        if (!starsBackgroundEnabled) {
            return Collections.emptyList();
        }
        return starsBackgroundPainter.getStarsSnapshot();
    }

    public void setGameImages() {
        shooterImage = loadImage("shooter", "/resources/Shooter/ShooterImage.png");
        invaderImage = loadImage("invader", "/resources/Invader/InvaderImage.png");
        // bulletImage starts null; falls back to triangle shape until user picks one
    }

    public void setShooterImageFromResourcePath(String resourcePath) {
        Image loadedImage = loadImage("shooter", resourcePath);
        if (loadedImage != null) {
            shooterImage = loadedImage;
        }
    }

    public void setInvaderImageFromResourcePath(String resourcePath) {
        Image loadedImage = loadImage("invader", resourcePath);
        if (loadedImage != null) {
            invaderImage = loadedImage;
        }
    }

    public void setBulletImageFromResourcePath(String resourcePath) {
        Image loadedImage = loadImage("bullet", resourcePath);
        if (loadedImage != null) {
            bulletImage = loadedImage;
        }
    }

    public void setBackgroundImageFromResourcePath(String resourcePath) {
        Image loadedImage = loadImage("background", resourcePath);
        if (loadedImage != null) {
            starsBackgroundEnabled = false;
            backgroundImage = loadedImage;
        }
    }

    public void enableStarsBackground(SpaceInvadersUI game) {
        starsBackgroundEnabled = true;
        backgroundImage = null;
        starsBackgroundPainter.attachGame(game);
        starsBackgroundPainter.startIfNeeded();
    }

    private static Image loadImage(String imageType, String resourcePath) {
        URL url = ImageSelection.class.getResource(resourcePath);
        if (url == null) {
            GameExceptions.showErrorDialog("Resource not found for " + imageType + ": " + resourcePath);
            return null;
        }
        // ImageIcon supports animated GIFs; ImageIO.read() only loads the first frame
        return new ImageIcon(url).getImage();
    }

    private static Image loadImageIfPresent(String resourcePath) {
        URL url = ImageSelection.class.getResource(resourcePath);
        if (url == null) {
            return null;
        }
        return new ImageIcon(url).getImage();
    }
}
