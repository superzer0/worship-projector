package sk.calvary.worship.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class UiThemeDisplayTest {
    @AfterEach
    void restoreLightTheme() {
        UiTheme.install(UiTheme.LIGHT);
    }

    @Test
    void refreshesDisplayableApplicationWindows() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless());
        AtomicReference<JFrame> frameRef = new AtomicReference<>();
        AtomicReference<JButton> buttonRef = new AtomicReference<>();

        try {
            SwingUtilities.invokeAndWait(() -> {
                UiTheme.install(UiTheme.LIGHT);
                JButton button = new JButton("GO LIVE");
                OperatorPreviewPanel panel = new OperatorPreviewPanel(
                        new JPanel(), new JPanel(), button, "PREPARED", "LIVE"
                );
                JFrame frame = new JFrame("theme-refresh-test");
                frame.setContentPane(panel);
                frame.pack();
                frame.setVisible(true);
                frameRef.set(frame);
                buttonRef.set(button);
            });

            SwingUtilities.invokeAndWait(() -> UiTheme.installAndRefresh(UiTheme.DARK));

            assertEquals(new Color(0xB91C1C), buttonRef.get().getBackground());
        } finally {
            if (frameRef.get() != null)
                SwingUtilities.invokeAndWait(frameRef.get()::dispose);
        }
    }
}
