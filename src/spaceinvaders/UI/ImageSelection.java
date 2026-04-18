package spaceinvaders.UI;

import spaceinvaders.GameExceptions;

import java.awt.*;
import java.net.URL;
import javax.swing.ImageIcon;

public class ImageSelection {
    private Image shooterImage;
    private Image invaderImage;
    private Image bulletImage;
    private Image backgroundImage;

    public Image getShooterImage() {
        return shooterImage;
    }

    public Image getInvaderImage() {
        return invaderImage;
    }

    public Image getBulletImage() {
        return bulletImage;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
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
            backgroundImage = loadedImage;
        }
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
}
