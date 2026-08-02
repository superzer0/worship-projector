package sk.calvary.worship.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.calvary.worship.ClickButton;
import sk.calvary.worship.FormatButton;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class LegacyControlStyleTest {
    @BeforeEach
    void installTheme() {
        UiTheme.install(UiTheme.LIGHT);
    }

    @Test
    void formattingControlsUseReadableTargetsAndThePreparedAccent() {
        Color preparedAccent = UIManager.getColor("jWorship.preparedAccent");

        FormatButton format = new FormatButton();
        format.setSelected(true);
        assertTrue(format.getPreferredSize().height >= 28);
        assertEquals(preparedAccent, format.getBackground());

        ClickButton click = new ClickButton();
        click.setSelected(true);
        assertTrue(click.getPreferredSize().height >= 28);
        assertEquals(preparedAccent, click.getBackground());
    }
}
