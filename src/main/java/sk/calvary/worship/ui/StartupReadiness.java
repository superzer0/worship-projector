package sk.calvary.worship.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StartupReadiness {
    static final String MARKER = "JWORSHIP_UI_READY";
    static final String READY_FILE_PROPERTY = "jworship.test.readyFile";

    private StartupReadiness() {
    }

    public static void signal() {
        System.out.println(MARKER);
        String readyFile = System.getProperty(READY_FILE_PROPERTY);
        if (readyFile == null || readyFile.isBlank())
            return;
        try {
            Files.writeString(Path.of(readyFile), MARKER, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write UI readiness marker " + readyFile, e);
        }
    }
}
