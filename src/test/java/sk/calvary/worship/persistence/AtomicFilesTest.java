package sk.calvary.worship.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtomicFilesTest {
    @TempDir
    Path tempDir;

    @Test
    void atomicallyReplacesTheTargetAndKeepsThePreviousVersion() throws Exception {
        Path target = tempDir.resolve("settings.ser");
        Files.writeString(target, "previous", StandardCharsets.UTF_8);

        AtomicFiles.write(target, output -> output.write("current".getBytes(StandardCharsets.UTF_8)));

        assertEquals("current", Files.readString(target, StandardCharsets.UTF_8));
        assertEquals("previous", Files.readString(AtomicFiles.backupPath(target), StandardCharsets.UTF_8));
        assertFalse(hasTemporarySibling(target));
    }

    @Test
    void leavesThePreviousFileUntouchedWhenWritingFails() throws Exception {
        Path target = tempDir.resolve("settings.ser");
        Files.writeString(target, "previous", StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> AtomicFiles.write(target, output -> {
            output.write("partial".getBytes(StandardCharsets.UTF_8));
            throw new IOException("simulated disk failure");
        }));

        assertEquals("previous", Files.readString(target, StandardCharsets.UTF_8));
        assertFalse(Files.exists(AtomicFiles.backupPath(target)));
        assertFalse(hasTemporarySibling(target));
    }

    @Test
    void createsMissingParentDirectories() throws Exception {
        Path target = tempDir.resolve("nested/settings.ser");

        AtomicFiles.write(target, output -> output.write(1));

        assertTrue(Files.isRegularFile(target));
    }

    private boolean hasTemporarySibling(Path target) throws IOException {
        try (var files = Files.list(target.getParent())) {
            String prefix = "." + target.getFileName() + ".";
            return files.anyMatch(path -> path.getFileName().toString().startsWith(prefix));
        }
    }
}
