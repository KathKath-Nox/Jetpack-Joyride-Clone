package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SoundSettingsTest {

    @Test
    void startsWithEverythingOnAtHalfVolume() {
        SoundSettings s = new SoundSettings();
        assertTrue(s.isMusicOn());
        assertTrue(s.isEffectsOn());
        assertEquals(SoundSettings.DEFAULT_VOLUME, s.getMusicVolume());
        assertTrue(s.isMusicAudible());
    }

    @Test
    void volumeStaysBetweenZeroAndAHundred() {
        SoundSettings s = new SoundSettings();
        s.setMusicVolume(250);
        assertEquals(100, s.getMusicVolume());
        s.setMusicVolume(-5);
        assertEquals(0, s.getMusicVolume());
        assertFalse(s.isMusicAudible());
    }

    @Test
    void volumeMovesByStepsAndConvertsToDecibels() {
        SoundSettings s = new SoundSettings();
        s.setMusicVolume(100);
        assertEquals(0f, s.getMusicGainDb(), 1e-4);
        s.changeMusicVolume(-5);
        assertEquals(50, s.getMusicVolume());
        assertEquals(-6.02f, s.getMusicGainDb(), 0.01);
    }

    @Test
    void mutedMusicIsNotAudible() {
        SoundSettings s = new SoundSettings();
        s.setMusicOn(false);
        assertFalse(s.isMusicAudible());
    }

    @Test
    void listenersAreOnlyNotifiedOnRealChanges() {
        SoundSettings s = new SoundSettings();
        int[] calls = {0};
        s.addListener(() -> calls[0]++);
        s.setMusicVolume(SoundSettings.DEFAULT_VOLUME);
        s.setEffectsOn(true);
        assertEquals(0, calls[0]);
        s.setEffectsOn(false);
        assertEquals(1, calls[0]);
    }

    @Test
    void settingsAreSavedAndReloaded(@TempDir Path dir) {
        Path file = dir.resolve("sub").resolve("sound.properties");
        SoundSettings first = new SoundSettings();
        first.persistTo(file);
        first.setMusicVolume(30);
        first.setEffectsOn(false);
        first.setMusicOn(false);

        SoundSettings second = new SoundSettings();
        second.persistTo(file);
        assertEquals(30, second.getMusicVolume());
        assertFalse(second.isEffectsOn());
        assertFalse(second.isMusicOn());
    }

    @Test
    void corruptFileKeepsDefaults(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("sound.properties");
        Files.writeString(file, "music.volume=abc\n");
        SoundSettings s = new SoundSettings();
        s.persistTo(file);
        assertEquals(SoundSettings.DEFAULT_VOLUME, s.getMusicVolume());
    }
}
