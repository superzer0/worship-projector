package sk.calvary.worship;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

final class LegacyObjectStreams {
    private LegacyObjectStreams() {
    }

    static Object read(File file) throws IOException, ClassNotFoundException {
        LegacyObjectInputFilter.checkStreamSize(file);
        return read(new BufferedInputStream(new FileInputStream(file)));
    }

    static Object read(InputStream source) throws IOException, ClassNotFoundException {
        try (InputStream ownedSource = source) {
            try (ObjectInputStream input = new PatchedObjectInputStream(ownedSource)) {
                return input.readObject();
            }
        }
    }
}
