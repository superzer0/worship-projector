package sk.calvary.worship.ui;

import java.awt.Dimension;
import java.awt.Rectangle;

public final class WindowSizing {
    private static final Dimension COMFORTABLE_SIZE = new Dimension(1200, 800);
    private static final Dimension MINIMUM_SIZE = new Dimension(800, 500);

    private WindowSizing() {
    }

    public static Dimension initialSize(Rectangle usableBounds) {
        return cap(COMFORTABLE_SIZE, usableBounds);
    }

    public static Dimension minimumSize(Rectangle usableBounds) {
        return cap(MINIMUM_SIZE, usableBounds);
    }

    private static Dimension cap(Dimension requested, Rectangle usableBounds) {
        if (usableBounds == null)
            return new Dimension(requested);
        return new Dimension(
                Math.max(1, Math.min(requested.width, usableBounds.width)),
                Math.max(1, Math.min(requested.height, usableBounds.height))
        );
    }
}
