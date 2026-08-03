package sk.calvary.worship;

import sk.calvary.worship.persistence.AtomicFiles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SongPersistenceTest {
    @TempDir
    Path tempDir;

    @Test
    void resavingKeepsTheFriendlyFilenameAndBacksUpThePreviousSong() throws Exception {
        Song song = song("Stable title", "Original verse");
        song.save(tempDir.toFile());
        File originalFile = song.getFile();

        song.setPlainText("Updated verse");
        song.save(tempDir.toFile());

        assertEquals(originalFile, song.getFile());
        assertEquals("Updated verse", Song.load(originalFile).getPlainText());
        Song backup = readSerialized(AtomicFiles.backupPath(originalFile.toPath()).toFile());
        assertEquals("Original verse", backup.getPlainText());
    }

    @Test
    void corruptPrimarySongRecoversFromItsLastKnownGoodBackup() throws Exception {
        Song song = song("Stable title", "first version");
        song.save(tempDir.toFile());
        song.setPlainText("second version");
        song.save(tempDir.toFile());
        File primary = song.getFile();

        Files.writeString(primary.toPath(), "not a serialized song");

        Song recovered = Song.load(primary);
        assertEquals("first version", recovered.getPlainText());

        try (ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(primary))) {
            output.writeObject(new HashMap<>());
        }
        assertEquals("first version", Song.load(primary).getPlainText());

        Files.delete(AtomicFiles.backupPath(primary.toPath()));
        assertEquals("first version", Song.load(primary).getPlainText());
    }

    @Test
    void successfulRenameInstallsTheNewSongBeforeRemovingTheOldName() throws Exception {
        Song song = song("Original title", "before");
        song.save(tempDir.toFile());
        File previousFile = song.getFile();

        song.setTitle("Renamed title");
        song.setPlainText("after");
        song.save(tempDir.toFile());

        assertFalse(previousFile.exists());
        assertTrue(song.getFile().isFile());
        assertEquals("renamed_title.sng", song.getFile().getName());
        assertEquals("after", Song.load(song.getFile()).getPlainText());

        Song backup = readSerialized(AtomicFiles.backupPath(song.getFile().toPath()).toFile());
        assertEquals("before", backup.getPlainText());
    }

    @Test
    void failedRenameSaveLeavesThePreviousFileAndSongReferenceUntouched() throws Exception {
        Song song = song("Original title", "Original verse");
        song.save(tempDir.toFile());
        File originalFile = song.getFile();
        byte[] originalBytes = Files.readAllBytes(originalFile.toPath());

        song.setTitle("Blocked");
        Path blockedTarget = tempDir.resolve("blocked.sng");
        Files.createDirectory(blockedTarget);
        Files.writeString(blockedTarget.resolve("keep"), "non-empty");

        assertThrows(IOException.class, () -> song.save(tempDir.toFile()));

        assertSame(originalFile, song.getFile());
        assertTrue(originalFile.isFile());
        assertEquals(java.util.Arrays.toString(originalBytes),
                java.util.Arrays.toString(Files.readAllBytes(originalFile.toPath())));
    }

    private Song song(String title, String verse) {
        Song song = new Song();
        song.setTitle(title);
        song.setTitle2("");
        song.setAuthor("Author");
        song.setPlainText(verse);
        return song;
    }

    private Song readSerialized(File file) throws Exception {
        try (ObjectInputStream input = new PatchedObjectInputStream(new FileInputStream(file))) {
            return (Song) input.readObject();
        }
    }
}
