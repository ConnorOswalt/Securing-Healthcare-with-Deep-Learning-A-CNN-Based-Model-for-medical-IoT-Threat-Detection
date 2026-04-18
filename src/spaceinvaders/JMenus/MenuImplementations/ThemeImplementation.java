package spaceinvaders.JMenus.MenuImplementations;

import spaceinvaders.GameExceptions;
import spaceinvaders.SpaceInvadersUI;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class ThemeImplementation {
    private static final Pattern STRING_FIELD_PATTERN_TEMPLATE =
            Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]+)\"");

    public void handleThemeSelection(ActionEvent e) {
        if (!(e.getSource() instanceof JMenuItem)) {
            return;
        }

        JMenuItem selectedItem = (JMenuItem) e.getSource();
        String selectedPath = selectedItem.getName();

        SpaceInvadersUI game = SpaceInvadersUI.getActiveInstance();
        if (game == null) {
            return;
        }

        if (selectedPath == null || selectedPath.isBlank()) {
            String customPath = JOptionPane.showInputDialog(
                    null,
                    "Enter theme JSON path (example: /resources/Themes/Retro.json):");

            if (customPath == null || customPath.isBlank()) {
                return;
            }

            selectedPath = normalizeResourcePath(customPath);
        }

        if (ThemeImplementation.class.getResource(selectedPath) == null) {
            GameExceptions.showErrorDialog("Theme file not found: " + selectedPath);
            return;
        }

        applyThemeFromJson(game, selectedPath);
    }

    private void applyThemeFromJson(SpaceInvadersUI game, String jsonResourcePath) {
        String jsonContent = readResourceFile(jsonResourcePath);
        if (jsonContent == null) {
            GameExceptions.showErrorDialog("Unable to read theme JSON: " + jsonResourcePath);
            return;
        }

        applyImagePath(game, jsonContent, "shooter", game.imageSelection::setShooterImageFromResourcePath);
        applyImagePath(game, jsonContent, "invader", game.imageSelection::setInvaderImageFromResourcePath);
        applyImagePath(game, jsonContent, "bullet", game.imageSelection::setBulletImageFromResourcePath);
        applyImagePath(game, jsonContent, "background", game.imageSelection::setBackgroundImageFromResourcePath);

        String musicPath = extractPath(jsonContent, "music");
        if (musicPath != null && game.getMusicHandler() != null) {
            game.getMusicHandler().selectTrack(musicPath);
        }

        game.repaint();
    }

    private void applyImagePath(SpaceInvadersUI game, String jsonContent, String key, ResourceApplier applier) {
        String resourcePath = extractPath(jsonContent, key);
        if (resourcePath == null) {
            return;
        }
        applier.apply(resourcePath);
    }

    private String extractPath(String jsonContent, String key) {
        String quotedKey = Pattern.quote(key);
        Pattern pattern = Pattern.compile(String.format(STRING_FIELD_PATTERN_TEMPLATE.pattern(), quotedKey));
        Matcher matcher = pattern.matcher(jsonContent);
        if (!matcher.find()) {
            return null;
        }

        String normalized = normalizeResourcePath(matcher.group(1));
        if (ThemeImplementation.class.getResource(normalized) == null) {
            GameExceptions.showErrorDialog("Theme resource missing for key '" + key + "': " + normalized);
            return null;
        }
        return normalized;
    }

    private String readResourceFile(String resourcePath) {
        try (InputStream input = ThemeImplementation.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return null;
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private String normalizeResourcePath(String path) {
        String normalized = path.trim().replace('\\', '/');
        if (normalized.startsWith("src/")) {
            normalized = normalized.substring(3);
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    @FunctionalInterface
    private interface ResourceApplier {
        void apply(String resourcePath);
    }
}
