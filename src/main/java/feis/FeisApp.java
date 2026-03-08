package feis;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

/**
 * Fire Equipment Inspection System
 * Entry point — boots FlatLaf dark theme and launches the main window.
 */
public class FeisApp {
    public static void main(String[] args) {
        // Install FlatLaf dark look-and-feel before any Swing component is created
        FlatDarkLaf.setup();

        // Fine-tune FlatLaf accent colour to match our navy/gold palette
        UIManager.put("Component.accentColor",          Theme.ACCENT);
        UIManager.put("Button.arc",                     10);
        UIManager.put("Component.arc",                  8);
        UIManager.put("TextComponent.arc",              6);
        UIManager.put("ScrollBar.thumbArc",             999);
        UIManager.put("ScrollBar.width",                10);
        UIManager.put("TabbedPane.selectedBackground",  Theme.PRIMARY_LIGHT);
        UIManager.put("TabbedPane.focusColor",          Theme.PRIMARY_LIGHT);

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
