package sk.calvary.worship.persistence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class AtomicFiles {
    private static final String BACKUP_SUFFIX = ".bak";

    private AtomicFiles() {
    }

    public static void write(Path target, OutputWriter writer) throws IOException {
        Path backupSource = Files.isRegularFile(target) ? target : null;
        write(target, backupSource, writer);
    }

    public static void write(Path target, Path backupSource, OutputWriter writer)
            throws IOException {
        writeInternal(target, backupSource, writer);
    }

    public static void restore(Path target, Path source) throws IOException {
        writeInternal(target, null, output -> Files.copy(source, output));
    }

    public static Path backupPath(Path target) {
        return target.resolveSibling(target.getFileName() + BACKUP_SUFFIX);
    }

    private static void writeInternal(Path target, Path backupSource,
                                      OutputWriter writer) throws IOException {
        Path absoluteTarget = target.toAbsolutePath();
        Path parent = absoluteTarget.getParent();
        if (parent == null)
            throw new IOException("Atomic file target has no parent: " + target);
        Files.createDirectories(parent);

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        writer.write(content);

        String prefix = "." + absoluteTarget.getFileName() + ".";
        Path temporary = Files.createTempFile(parent, prefix, ".tmp");
        try {
            writeAndSync(temporary, content.toByteArray());
            if (backupSource != null && Files.isRegularFile(backupSource))
                replaceBackup(backupSource.toAbsolutePath(), backupPath(absoluteTarget));
            moveReplacing(temporary, absoluteTarget);
            forceDirectory(parent);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void replaceBackup(Path source, Path backup) throws IOException {
        Path parent = backup.getParent();
        String prefix = "." + backup.getFileName() + ".";
        Path temporaryBackup = Files.createTempFile(parent, prefix, ".tmp");
        try {
            Files.copy(source, temporaryBackup, StandardCopyOption.REPLACE_EXISTING);
            forceFile(temporaryBackup);
            moveReplacing(temporaryBackup, backup);
        } finally {
            Files.deleteIfExists(temporaryBackup);
        }
    }

    private static void writeAndSync(Path path, byte[] content) throws IOException {
        try (FileChannel channel = FileChannel.open(path,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            ByteBuffer buffer = ByteBuffer.wrap(content);
            while (buffer.hasRemaining())
                channel.write(buffer);
            channel.force(true);
        }
    }

    private static void forceFile(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            channel.force(true);
        }
    }

    private static void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void forceDirectory(Path directory) {
        try (FileChannel channel = FileChannel.open(directory, StandardOpenOption.READ)) {
            channel.force(true);
        } catch (IOException ignored) {
            // Directory fsync is not supported by every desktop filesystem/provider.
        }
    }

    @FunctionalInterface
    public interface OutputWriter {
        void write(OutputStream output) throws IOException;
    }
}
