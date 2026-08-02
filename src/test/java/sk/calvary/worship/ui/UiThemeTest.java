package sk.calvary.worship.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

class UiThemeTest {
    @AfterEach
    void restoreLightTheme() {
        UiTheme.install(UiTheme.LIGHT);
    }

    @Test
    void resolvesPersistedThemeNamesConservatively() {
        assertEquals(UiTheme.LIGHT, UiTheme.fromSetting(null));
        assertEquals(UiTheme.LIGHT, UiTheme.fromSetting("unknown"));
        assertEquals(UiTheme.LIGHT, UiTheme.fromSetting("light"));
        assertEquals(UiTheme.DARK, UiTheme.fromSetting("DARK"));
    }

    @Test
    void installsModernLightThemeAndSemanticDefaults() {
        UiTheme.install(UiTheme.LIGHT);

        assertInstanceOf(FlatLightLaf.class, UIManager.getLookAndFeel());
        assertEquals(10, UIManager.getInt("Button.arc"));
        assertEquals(8, UIManager.getInt("Component.arc"));
        assertNotNull(UIManager.getColor("jWorship.preparedAccent"));
        assertNotNull(UIManager.getColor("jWorship.liveAccent"));
    }

    @Test
    void installsDarkTheme() {
        UiTheme.install(UiTheme.DARK);

        assertInstanceOf(FlatDarkLaf.class, UIManager.getLookAndFeel());
        assertEquals(new Color(0x60A5FA), UIManager.getColor("jWorship.preparedAccent"));
    }

    @Test
    void darkThemeUsesAHighContrastLiveActionColor() {
        UiTheme.install(UiTheme.DARK);

        assertEquals(new Color(0xB91C1C),
                UIManager.getColor("jWorship.liveActionBackground"));
    }

    @Test
    void fallsBackToTheSystemLookAndFeelWhenFlatLafCannotBeInstalled() {
        boolean installed = UiTheme.installWithFallback(UiTheme.DARK, () -> {
            throw new IllegalStateException("simulated FlatLaf failure");
        });

        assertFalse(installed);
        assertEquals(UIManager.getSystemLookAndFeelClassName(),
                UIManager.getLookAndFeel().getClass().getName());
    }
}
