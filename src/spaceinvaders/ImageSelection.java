package spaceinvaders;

import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageSelection {
    private Image shooterImage;
    private Image invaderImage;

    public Image getShooterImage() {
        return shooterImage;
    }

    public Image getInvaderImage() {
        return invaderImage;
    }

    public void setGameImages() {
        shooterImage = loadImage("shooter",
         "/spaceinvaders/resources/Shooter/ShooterImage.png");
        invaderImage = loadImage("invader",
                "/spaceinvaders/resources/Invader/InvaderImage.png");
    }

    private static Image loadImage(String imageType, String defaultResourcePath) {
        try {
            return ImageIO.read(ImageSelection.class.getResource(defaultResourcePath));
        } catch (IOException e) {
            GameExceptions.showErrorDialog("Failed to load default " + imageType + " image: " + e.getMessage());
        }

        return null;
    }
}
