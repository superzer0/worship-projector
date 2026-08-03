/*
 * Created on 12.8.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sk.calvary.worship;

import sk.calvary.worship.persistence.AtomicFiles;

import java.io.*;

public class SafeFileOutputStream extends OutputStream {

    final File file;

    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    boolean closed = false;

    SafeFileOutputStream(File f) {
        file = f;
    }

    public static void safeSave(File f, Object o) throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(
                new SafeFileOutputStream(f));
        os.writeObject(o);
        os.close();
    }

    public static Object safeLoad(File f, Object defaultValue)
            throws IOException, ClassNotFoundException {
        if (!f.exists())
            return defaultValue;
        try {
            return readSerialized(f);
        } catch (IOException | ClassNotFoundException primaryFailure) {
            File backup = AtomicFiles.backupPath(f.toPath()).toFile();
            if (!backup.isFile())
                throw primaryFailure;
            Object recovered;
            try {
                recovered = readSerialized(backup);
            } catch (IOException | ClassNotFoundException backupFailure) {
                primaryFailure.addSuppressed(backupFailure);
                throw primaryFailure;
            }
            AtomicFiles.restore(f.toPath(), backup.toPath());
            return recovered;
        }
    }

    private static Object readSerialized(File file)
            throws IOException, ClassNotFoundException {
        ObjectInputStream s = new PatchedObjectInputStream(new FileInputStream(file));
        try {
            return s.readObject();
        } finally {
            s.close();
        }
    }

    @Override
    public void write(int b) throws IOException {
        buffer.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        buffer.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (closed)
            return;
        try {
            AtomicFiles.write(file.toPath(), output -> buffer.writeTo(output));
        } finally {
            closed = true;
        }
    }
}
