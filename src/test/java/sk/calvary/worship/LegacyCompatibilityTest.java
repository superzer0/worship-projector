package sk.calvary.worship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sk.calvary.misc.Lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LegacyCompatibilityTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsSongSerializedByTheOriginalSkAscRelease() throws Exception {
        File fixture = copyFixture("/compat/songs/sk-asc-legacy-song.sng");

        Song song = Song.load(fixture);

        assertEquals("Legacy Title", song.getTitle());
        assertEquals("Legacy Alternate", song.getTitle2());
        assertEquals("Legacy Author", song.getAuthor());
        assertEquals(2, song.getVerseCount());
        assertEquals("First legacy verse\nline two\n@Second legacy verse", song.getPlainText());
    }

    @Test
    void loadsHistoricalPlainTextSong() throws Exception {
        File fixture = copyFixture("/compat/songs/legacy-song.txt");

        Song song = Song.load(fixture);

        assertEquals("legacy-song", song.getTitle());
        assertEquals(2, song.getVerseCount());
        assertEquals("Prvá sloha\n@Druhá sloha", song.getPlainText());
    }

    @Test
    void fallsBackToWindows1250ForHistoricalPlainTextSongs() throws Exception {
        File fixture = copyFixture("/compat/songs/legacy-song-cp1250.txt");

        Song song = Song.load(fixture);

        assertEquals("Žltá pieseň\n@Druhá sloha", song.getPlainText());
    }

    @Test
    void loadsPreModernizationGeneralSettings() throws Exception {
        File fixture = copyFixture("/compat/settings/generalSettings.ser");

        Object loaded = SafeFileOutputStream.safeLoad(fixture, new HashMap<Integer, String>());

        @SuppressWarnings("unchecked")
        HashMap<Integer, String> settings = (HashMap<Integer, String>) loaded;
        assertEquals("sk", settings.get(1));
        assertEquals("dark", settings.get(2));
    }

    @Test
    void loadsPreModernizationPictureBookmarks() throws Exception {
        File fixture = copyFixture("/compat/settings/picturebookmarks.ser");

        PictureBookmarksList loaded = (PictureBookmarksList) SafeFileOutputStream.safeLoad(
                fixture, new PictureBookmarksList());
        loaded.updateOwnership();

        PictureBookmarks[] groups = loaded.getBookmarks();
        assertEquals(1, groups.length);
        assertEquals("Legacy backgrounds", groups[0].getName());
        assertEquals(1, groups[0].getBookmarks().length);
        assertEquals("legacy-background.jpg", groups[0].getBookmarks()[0].getValue());
    }

    @Test
    void parsesCustomLanguageFilesWithMissingModernKeys() throws Exception {
        try (InputStream input = resource("/compat/lang/custom-lang.lng")) {
            Lang lang = Lang.parse(input);

            assertEquals("Legacy Go", lang.getString("#1000", "en"));
            assertEquals("Legacy Choď", lang.getString("#1000", "sk"));
            assertEquals("Fallback label", lang.getString("#9999", "en"));
        }
    }

    private File copyFixture(String name) throws IOException {
        Path target = tempDir.resolve(Path.of(name).getFileName().toString());
        try (InputStream input = resource(name)) {
            Files.copy(input, target);
        }
        return target.toFile();
    }

    private InputStream resource(String name) throws IOException {
        InputStream input = LegacyCompatibilityTest.class.getResourceAsStream(name);
        assertNotNull(input, "Missing compatibility fixture " + name);
        return input;
    }
}
