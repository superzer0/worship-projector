package sk.calvary.worship.ui;

import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowSizingTest {
    @Test
    void capsTheInitialAndMinimumSizesToASmallLogicalDesktop() {
        Rectangle usableBounds = new Rectangle(0, 0, 960, 540);

        Dimension initial = WindowSizing.initialSize(usableBounds);
        Dimension minimum = WindowSizing.minimumSize(usableBounds);

        assertEquals(new Dimension(960, 540), initial);
        assertTrue(minimum.width <= usableBounds.width);
        assertTrue(minimum.height <= usableBounds.height);
        assertTrue(minimum.width < 1000);
        assertTrue(minimum.height < 650);
    }

    @Test
    void retainsTheComfortableDefaultOnALargeDesktop() {
        Rectangle usableBounds = new Rectangle(20, 30, 1920, 1040);

        assertEquals(new Dimension(1200, 800), WindowSizing.initialSize(usableBounds));
        assertEquals(new Dimension(800, 500), WindowSizing.minimumSize(usableBounds));
    }
}
