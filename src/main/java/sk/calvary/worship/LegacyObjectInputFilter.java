package sk.calvary.worship;

import java.io.ObjectInputFilter;
import java.util.Set;

final class LegacyObjectInputFilter implements ObjectInputFilter {
    static final LegacyObjectInputFilter INSTANCE = new LegacyObjectInputFilter();

    private static final long MAX_BYTES = 16L * 1024L * 1024L;
    private static final long MAX_REFERENCES = 100_000L;
    private static final long MAX_ARRAY_LENGTH = 100_000L;
    private static final long MAX_DEPTH = 64L;

    private static final Set<String> ALLOWED_CLASSES = Set.of(
            "java.lang.Object",
            "java.lang.String",
            "java.lang.Number",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.Byte",
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.Float",
            "java.lang.Double",
            "java.util.Vector",
            "java.util.HashMap",
            "java.util.Map$Entry",
            "java.util.Calendar",
            "java.util.GregorianCalendar",
            "java.util.TimeZone",
            "java.util.SimpleTimeZone",
            "sun.util.calendar.ZoneInfo",
            "sk.calvary.worship.Song",
            "sk.calvary.worship.Bookmark",
            "sk.calvary.worship.Bookmarks",
            "sk.calvary.worship.BookmarksList",
            "sk.calvary.worship.PictureBookmark",
            "sk.calvary.worship.PictureBookmarks",
            "sk.calvary.worship.PictureBookmarksList"
    );

    private LegacyObjectInputFilter() {
    }

    @Override
    public Status checkInput(FilterInfo info) {
        if (info.depth() > MAX_DEPTH
                || info.references() > MAX_REFERENCES
                || info.streamBytes() > MAX_BYTES
                || info.arrayLength() > MAX_ARRAY_LENGTH)
            return Status.REJECTED;

        Class<?> serialClass = info.serialClass();
        if (serialClass == null)
            return Status.UNDECIDED;

        while (serialClass.isArray())
            serialClass = serialClass.getComponentType();
        if (serialClass.isPrimitive())
            return Status.ALLOWED;

        return ALLOWED_CLASSES.contains(serialClass.getName())
                ? Status.ALLOWED
                : Status.REJECTED;
    }
}
