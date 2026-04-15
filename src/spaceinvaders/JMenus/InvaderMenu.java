package spaceinvaders.JMenus;

import java.util.Arrays;

public class InvaderMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Invader";
    }

    @Override
    protected java.util.ArrayList<String> setButtonTitles() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                "Invader1", "Invader2", "Invader3", "Invader4"));
    }
}