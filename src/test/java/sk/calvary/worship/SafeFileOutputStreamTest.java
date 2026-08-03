package sk.calvary.worship;

import sk.calvary.worship.persistence.AtomicFiles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeFileOutputStreamTest {
    @TempDir
    Path tempDir;

    @Test
    void safeSaveKeepsThePreviousSerializedValueAsABackup() throws Exception {
        File target = tempDir.resolve("generalSettings.ser").toFile();
        SafeFileOutputStream.safeSave(target, settings("light"));

        SafeFileOutputStream.safeSave(target, settings("dark"));

        assertEquals("dark", loadTheme(target));
        assertEquals("light", loadTheme(AtomicFiles.backupPath(target.toPath()).toFile()));
    }

    @Test
    void safeLoadRecoversAValidBackupWhenThePrimaryIsCorrupt() throws Exception {
        File target = tempDir.resolve("generalSettings.ser").toFile();
        SafeFileOutputStream.safeSave(target, settings("light"));
        SafeFileOutputStream.safeSave(target, settings("dark"));
        Files.writeString(target.toPath(), "corrupt serialized data");

        assertEquals("light", loadTheme(target));

        Files.delete(AtomicFiles.backupPath(target.toPath()));
        assertEquals("light", loadTheme(target));
    }

    @Test
    void missingFilesStillReturnTheSuppliedDefault() throws Exception {
        HashMap<Integer, String> defaults = settings("light");

        Object loaded = SafeFileOutputStream.safeLoad(tempDir.resolve("missing.ser").toFile(), defaults);

        assertTrue(loaded == defaults);
    }

    private String loadTheme(File file) throws Exception {
        @SuppressWarnings("unchecked")
        HashMap<Integer, String> loaded = (HashMap<Integer, String>) SafeFileOutputStream.safeLoad(
                file, new HashMap<Integer, String>());
        return loaded.get(2);
    }

    private HashMap<Integer, String> settings(String theme) {
        HashMap<Integer, String> value = new HashMap<Integer, String>();
        value.put(1, "en");
        value.put(2, theme);
        return value;
    }
}
