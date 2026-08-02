package sk.calvary.worship.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class OperatorPreviewPanelTest {
    @BeforeEach
    void installTheme() {
        UiTheme.install(UiTheme.LIGHT);
    }

    @Test
    void presentsPreparedAndLiveViewsWithAnOperatorSizedAction() throws Exception {
        AtomicReference<OperatorPreviewPanel> panelRef = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> panelRef.set(new OperatorPreviewPanel(
                new JPanel(),
                new JPanel(),
                new JButton("GO LIVE"),
                "PREPARED",
                "LIVE"
        )));

        OperatorPreviewPanel panel = panelRef.get();
        assertNotNull(findByName(panel, "preparedPreview"));
        assertNotNull(findByName(panel, "livePreview"));

        Component action = findByName(panel, "goLiveButton");
        assertInstanceOf(JButton.class, action);
        assertTrue(action.getPreferredSize().height >= 44);
        assertTrue(panel.getMinimumSize().width >= 260);
    }

    @Test
    void refreshesSemanticColorsWhenTheThemeChanges() throws Exception {
        AtomicReference<OperatorPreviewPanel> panelRef = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> panelRef.set(new OperatorPreviewPanel(
                new JPanel(),
                new JPanel(),
                new JButton("GO LIVE"),
                "PREPARED",
                "LIVE"
        )));

        OperatorPreviewPanel panel = panelRef.get();
        JButton action = (JButton) findByName(panel, "goLiveButton");
        assertEquals(new Color(0xDC2626), action.getBackground());

        SwingUtilities.invokeAndWait(() -> {
            UiTheme.install(UiTheme.DARK);
            SwingUtilities.updateComponentTreeUI(panel);
        });

        assertEquals(new Color(0xB91C1C), action.getBackground());
    }

    @Test
    void keepsTheCriticalControlsVisibleInACompactPreviewColumn() throws Exception {
        AtomicReference<OperatorPreviewPanel> panelRef = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            OperatorPreviewPanel panel = new OperatorPreviewPanel(
                    new JPanel(),
                    new JPanel(),
                    new JButton("GO LIVE"),
                    "PREPARED",
                    "LIVE"
            );
            panel.setSize(300, 480);
            layoutRecursively(panel);
            panelRef.set(panel);
        });

        OperatorPreviewPanel panel = panelRef.get();
        Component prepared = findByName(panel, "preparedPreview");
        Component live = findByName(panel, "livePreview");
        Component action = findByName(panel, "goLiveButton");
        assertTrue(prepared.isVisible() && prepared.getHeight() >= 120);
        assertTrue(live.isVisible() && live.getHeight() >= 120);
        assertTrue(action.isVisible() && action.getHeight() >= 44);
    }

    private static void layoutRecursively(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container nested)
                layoutRecursively(nested);
        }
    }

    private static Component findByName(Container root, String name) {
        if (name.equals(root.getName()))
            return root;
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName()))
                return child;
            if (child instanceof Container container) {
                Component match = findByName(container, name);
                if (match != null)
                    return match;
            }
        }
        return null;
    }
}
