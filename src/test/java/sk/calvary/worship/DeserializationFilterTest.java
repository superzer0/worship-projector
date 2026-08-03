package sk.calvary.worship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DeserializationFilterTest {
    @TempDir
    Path tempDir;

    @Test
    void rejectsClassesOutsideTheLegacyDataAllowlist() throws Exception {
        Path file = tempDir.resolve("unexpected.ser");
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(file))) {
            output.writeObject(new UnexpectedPayload("do not load"));
        }

        assertThrows(InvalidClassException.class,
                () -> SafeFileOutputStream.safeLoad(file.toFile(), "default"));
    }

    private record UnexpectedPayload(String value) implements Serializable {
    }
}
