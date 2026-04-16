package spaceinvaders.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;
import java.util.Arrays;

public class InvaderMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Invader";
    }

    @Override
    protected java.util.ArrayList<String> setButtonTitles() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                "Alien", "Retro", "Tiger Tank", "Custom"));
    }

    @Override
    protected java.util.ArrayList<String> setButtonPaths() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                "/spaceinvaders/resources/Invader/InvaderImage.png",
                "/spaceinvaders/resources/Invader/Alien1.png",
                "/spaceinvaders/resources/Invader/TigerII-Invader.png"));
    }
    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().invaderMenuListener();
    }
}