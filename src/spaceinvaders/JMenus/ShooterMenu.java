package spaceinvaders.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;
import java.util.Arrays;

public class ShooterMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Shooter";
    }

    @Override
    protected java.util.ArrayList<String> setButtonTitles() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                "Smile", "Retro", "Sherman Tank", "custom"));
    }

    @Override
    protected java.util.ArrayList<String> setButtonPaths() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                "/spaceinvaders/resources/Shooter/ShooterImage.png",
                "/spaceinvaders/resources/Shooter/Retro.png",
                "/spaceinvaders/resources/Shooter/Sherman-Shooter.png"));
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().shooterMenuListener();
    }
}