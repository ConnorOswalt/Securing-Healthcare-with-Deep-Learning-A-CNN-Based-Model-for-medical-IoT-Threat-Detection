package spaceinvaders.JMenus;

import java.util.Arrays;

public class ShooterMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Shooter";
    }

    @Override
    protected java.util.ArrayList<String> setButtonTitles() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                "Shooter1", "Shooter2", "Shooter3", "Shooter4"));
    }
}