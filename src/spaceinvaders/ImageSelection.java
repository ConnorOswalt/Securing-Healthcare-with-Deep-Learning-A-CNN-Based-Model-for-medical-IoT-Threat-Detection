package spaceinvaders;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;
import javax.swing.*;

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
        String imageUrl = JOptionPane.showInputDialog(null,
                "Enter URL for " + imageType + " image (or leave blank for default):");

        // Need to handle case where url is not an image, ie a png or jpeg.
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                URI uri = new URI(imageUrl);
                return ImageIO.read(uri.toURL());
            } catch (IllegalArgumentException e) {
                GameExceptions.showErrorDialog(
                        "Illegal Argument " + imageUrl + " : " + e.getMessage()
                                + "\nLoading default image instead");
            } catch (URISyntaxException e) {
                GameExceptions.showErrorDialog(
                        "URL Syntax error for " + imageUrl + " : " + e.getMessage()
                                + "\nLoading default image instead");
            } catch (MalformedURLException e) {
                GameExceptions.showErrorDialog(
                        "Malformed URL for " + imageUrl + " : " + e.getMessage()
                                + "\nLoading default image instead");
            } catch (IOException e) {
                GameExceptions.showErrorDialog(
                        "Failed to load " + imageUrl + " : " + e.getMessage()
                                + "\nLoading default image instead");
            }
        }

        // If no URL is provided or URL fails, load the default resource
        try {
            return ImageIO.read(ImageSelection.class.getResource(defaultResourcePath));
        } catch (IOException e) {
            GameExceptions.showErrorDialog("Failed to load default " + imageType + " image: " + e.getMessage());
        }

        return null;
    }
}
