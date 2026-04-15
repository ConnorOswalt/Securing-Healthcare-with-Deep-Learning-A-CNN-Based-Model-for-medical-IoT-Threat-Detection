package spaceinvaders.JMenus;

import java.util.Arrays;

public class MusicMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Music";
    }

    @Override
    protected java.util.ArrayList<String> setButtonTitles() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                "Music1", "Music2", "Music3", "Music4"));
    }
}