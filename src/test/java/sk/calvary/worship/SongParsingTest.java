package sk.calvary.worship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SongParsingTest {

    @Test
    void plainTextRoundTripPreservesVerseBoundariesAndDiacritics() {
        Song song = new Song();

        song.setPlainText("Prvá sloha\r\n@Druhá sloha");

        assertEquals(2, song.getVerseCount());
        assertEquals("Prvá sloha\n@Druhá sloha", song.getPlainText());
    }
}
