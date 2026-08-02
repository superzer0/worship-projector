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
        assertTrue(panel.getMinimumSize().width >= 300);
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
