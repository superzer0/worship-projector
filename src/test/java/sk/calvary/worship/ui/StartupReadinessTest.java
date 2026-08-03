package sk.calvary.worship.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StartupReadinessTest {
    @TempDir
    Path tempDir;

    @Test
    void writesTheReadyMarkerForPackagedSmokeTests() throws Exception {
        Path readyFile = tempDir.resolve("ui-ready.txt");
        String previous = System.getProperty(StartupReadiness.READY_FILE_PROPERTY);
        System.setProperty(StartupReadiness.READY_FILE_PROPERTY, readyFile.toString());
        try {
            StartupReadiness.signal();
        } finally {
            if (previous == null)
                System.clearProperty(StartupReadiness.READY_FILE_PROPERTY);
            else
                System.setProperty(StartupReadiness.READY_FILE_PROPERTY, previous);
        }

        assertEquals(StartupReadiness.MARKER, Files.readString(readyFile));
    }
}
