package spaceinvaders.JMenus.MenuImplementations;

import spaceinvaders.GameExceptions;
import spaceinvaders.SpaceInvadersUI;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class BulletImplementation {
	private static String selectedBulletType = "Triangle";
	private static String selectedBulletPath;

	public static synchronized String getSelectedBulletType() {
		return selectedBulletType;
	}

	public static synchronized String getSelectedBulletPath() {
		return selectedBulletPath;
	}

	private static synchronized void setSelection(String bulletType, String bulletPath) {
		selectedBulletType = bulletType;
		selectedBulletPath = bulletPath;
	}

	public void handleBulletSelection(ActionEvent e) {
		if (!(e.getSource() instanceof JMenuItem)) {
			return;
		}

		JMenuItem selectedItem = (JMenuItem) e.getSource();
		String selectedTitle = selectedItem.getText();
		String selectedPath = selectedItem.getName();

		if (selectedTitle == null) {
			return;
		}

		switch (selectedTitle) {
			case "Triangle":
				// Triangle currently maps directly to drawBullets' default behavior.
				setSelection("Triangle", null);
				break;
			case "Circle":
				// Circle is tracked here for PaintingActions to render as red circle.
				setSelection("Circle", null);
				break;
			case "Bullet":
				if (selectedPath == null || selectedPath.isBlank()) {
					throw new IllegalArgumentException("Bullet menu option requires a valid image path.");
				}
				setSelection("Bullet", selectedPath.trim());
				break;
			case "Rocket":
				if (selectedPath == null || selectedPath.isBlank()) {
					throw new IllegalArgumentException("Rocket menu option requires a valid image path.");
				}
				setSelection("Rocket", selectedPath.trim());
				break;
			case "Custom":
				String customPath = JOptionPane.showInputDialog(
						null,
						"Enter project resource path (example: /spaceinvaders/resources/Bullets/Rocket.png):");

				if (customPath == null || customPath.isBlank()) {
					return;
				}

				String normalizedPath = normalizeResourcePath(customPath);
				if (!isValidResourcePath(normalizedPath)) {
					GameExceptions.showErrorDialog("Invalid project resource path: " + customPath);
					return;
				}

				setSelection("Custom", normalizedPath);
				break;
			default:
				throw new IllegalArgumentException("Unsupported bullet option: " + selectedTitle);
		}

		SpaceInvadersUI game = SpaceInvadersUI.getActiveInstance();
		if (game != null) {
			game.repaint();
		}
	}

	private String normalizeResourcePath(String customPath) {
		String normalized = customPath.trim().replace('\\', '/');
		if (normalized.startsWith("src/")) {
			normalized = normalized.substring(3);
		}
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}
		return normalized;
	}

	private boolean isValidResourcePath(String normalizedPath) {
		if (normalizedPath == null || normalizedPath.isBlank()) {
			return false;
		}

		if (BulletImplementation.class.getResource(normalizedPath) != null) {
			return true;
		}

		if (normalizedPath.startsWith("/")) {
			return BulletImplementation.class.getClassLoader().getResource(normalizedPath.substring(1)) != null;
		}

		return BulletImplementation.class.getClassLoader().getResource(normalizedPath) != null;
	}
}