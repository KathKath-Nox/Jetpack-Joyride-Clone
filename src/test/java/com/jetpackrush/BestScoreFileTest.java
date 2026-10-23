package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BestScoreFileTest {

    @TempDir
    Path dir;

    @Test
    void recordIsReloadedByANewInstance() {
        Path file = dir.resolve("score.txt");
        new BestScore(file).submit(120);
        assertEquals(120, new BestScore(file).get());
    }

    @Test
    void missingFileGivesZero() {
        assertEquals(0, new BestScore(dir.resolve("absent.txt")).get());
    }

    @Test
    void invalidContentGivesZero() throws IOException {
        Path file = dir.resolve("score.txt");
        Files.writeString(file, "abc");
        assertEquals(0, new BestScore(file).get());
    }

    @Test
    void negativeContentGivesZero() throws IOException {
        Path file = dir.resolve("score.txt");
        Files.writeString(file, "-5");
        assertEquals(0, new BestScore(file).get());
    }

    @Test
    void lowerScoreDoesNotOverwriteTheFile() throws IOException {
        Path file = dir.resolve("score.txt");
        new BestScore(file).submit(100);
        BestScore reloaded = new BestScore(file);
        reloaded.submit(50);
        assertEquals("100", Files.readString(file));
        assertEquals(100, reloaded.get());
    }

    @Test
    void parentDirectoryIsCreated() {
        Path file = dir.resolve("a").resolve("b").resolve("score.txt");
        new BestScore(file).submit(30);
        assertTrue(Files.exists(file));
    }

    @Test
    void unwritableLocationDoesNotCrash() throws IOException {
        Path blocker = dir.resolve("blocker");
        Files.writeString(blocker, "x");
        BestScore score = new BestScore(blocker.resolve("score.txt"));
        score.submit(10);
        assertEquals(10, score.get());
    }

    @Test
    void inMemoryScoreWritesNothing() {
        BestScore score = new BestScore();
        score.submit(40);
        assertEquals(40, score.get());
    }
}
