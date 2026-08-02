package sk.calvary.worship.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public enum UiTheme {
    LIGHT("Light"),
    DARK("Dark");

    private final String displayName;

    UiTheme(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSettingValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static UiTheme fromSetting(String value) {
        if (value == null)
            return LIGHT;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return LIGHT;
        }
    }

    public static void install(UiTheme theme) {
        UiTheme selected = theme == null ? LIGHT : theme;
        FlatLaf lookAndFeel = selected == DARK ? new FlatDarkLaf() : new FlatLightLaf();
        if (!FlatLaf.setup(lookAndFeel))
            throw new IllegalStateException("Unable to install the " + selected.displayName + " theme");

        applyDefaults(selected);
    }

    public static boolean installWithFallback(UiTheme theme) {
        return installWithFallback(theme, () -> install(theme));
    }

    static boolean installWithFallback(UiTheme theme, Runnable themeInstaller) {
        UiTheme selected = theme == null ? LIGHT : theme;
        try {
            themeInstaller.run();
            return true;
        } catch (RuntimeException primaryFailure) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                applyDefaults(selected);
                return false;
            } catch (Exception fallbackFailure) {
                fallbackFailure.addSuppressed(primaryFailure);
                throw new IllegalStateException("Unable to install a Swing look and feel", fallbackFailure);
            }
        }
    }

    private static void applyDefaults(UiTheme selected) {
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 8);
        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Component.innerFocusWidth", 1);
        UIManager.put("ScrollBar.width", 14);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabHeight", 36);
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("jWorship.preparedAccent",
                selected == DARK ? new Color(0x60A5FA) : new Color(0x2563EB));
        UIManager.put("jWorship.liveAccent",
                selected == DARK ? new Color(0xF87171) : new Color(0xDC2626));
        UIManager.put("jWorship.liveActionBackground",
                selected == DARK ? new Color(0xB91C1C) : new Color(0xDC2626));
    }

    public static void installAndRefresh(UiTheme theme) {
        installWithFallback(theme);
        for (Window window : Window.getWindows())
            SwingUtilities.updateComponentTreeUI(window);
    }
}