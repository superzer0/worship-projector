package sk.calvary.worship;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyObjectStreamsTest {
    @Test
    void closesTheOwnedInputWhenTheSerializationHeaderIsInvalid() {
        CloseTrackingInputStream input = new CloseTrackingInputStream("not serialized".getBytes());

        assertThrows(IOException.class, () -> LegacyObjectStreams.read(input));

        assertTrue(input.closed);
    }

    private static final class CloseTrackingInputStream extends ByteArrayInputStream {
        private boolean closed;

        private CloseTrackingInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
