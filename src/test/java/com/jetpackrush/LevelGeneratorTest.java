package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class LevelGeneratorTest {
    private final GameSpeed speed = new GameSpeed(500);

    @Test
    void defaultChunksAreAllPassable() {
        for (Chunk c : LevelGenerator.defaultChunks()) {
            assertTrue(c.isPassable(), c.getName());
        }
    }

    @Test
    void rejectsAnImpassableChunk() {
        Chunk wall = new Chunk("mur", 50, List.of(new Chunk.Rect(0, 0, 50, Chunk.PLAY_BOTTOM)));
        assertThrows(IllegalArgumentException.class,
            () -> new LevelGenerator(speed, new Random(1), List.of(wall)));
    }

    @Test
    void rejectsAnEmptyChunkList() {
        assertThrows(IllegalArgumentException.class,
            () -> new LevelGenerator(speed, new Random(1), List.of()));
    }

    @Test
    void nothingSpawnsBeforeTheFirstDistance() {
        LevelGenerator gen = LevelGenerator.withDefaultChunks(speed, new Random(1));
        assertTrue(gen.update(0.5).isEmpty());
        assertFalse(gen.update(1.0).isEmpty());
    }

    @Test
    void chunkAppearsAtTheSpawnLine() {
        Chunk chunk = new Chunk("c", 60, List.of(new Chunk.Rect(0, 100, 60, 60)));
        LevelGenerator gen = new LevelGenerator(speed, new Random(1), List.of(chunk));
        List<Obstacle> obstacles = gen.update(LevelGenerator.FIRST_DISTANCE / speed.getSpeed());
        assertEquals(LevelGenerator.SPAWN_X, obstacles.get(0).getX(), 1e-9);
    }

    @Test
    void chunksAreSeparatedByTheGap() {
        Chunk chunk = new Chunk("c", 60, List.of(new Chunk.Rect(0, 100, 60, 60)));
        LevelGenerator gen = new LevelGenerator(speed, new Random(1), List.of(chunk));
        double dt = 0.01;
        double scrolled = 0;
        double firstAt = -1;
        double secondAt = -1;
        for (int i = 0; i < 2000 && secondAt < 0; i++) {
            List<Obstacle> spawned = gen.update(dt);
            scrolled += speed.getSpeed() * dt;
            if (!spawned.isEmpty()) {
                if (firstAt < 0) {
                    firstAt = scrolled - (LevelGenerator.SPAWN_X - spawned.get(0).getX());
                } else {
                    secondAt = scrolled - (LevelGenerator.SPAWN_X - spawned.get(0).getX());
                }
            }
        }
        assertEquals(60 + LevelGenerator.GAP, secondAt - firstAt, 1e-6);
    }

    @Test
    void neverRepeatsTheSameChunkTwiceInARow() {
        LevelGenerator gen = LevelGenerator.withDefaultChunks(speed, new Random(42));
        String previous = null;
        int seen = 0;
        for (int i = 0; i < 20000 && seen < 50; i++) {
            List<Obstacle> spawned = gen.update(0.02);
            if (!spawned.isEmpty()) {
                String signature = spawned.size() + ":" + spawned.get(0).getHitbox().getWidth()
                    + ":" + spawned.get(0).getY();
                assertFalse(signature.equals(previous));
                previous = signature;
                seen++;
            }
        }
        assertEquals(50, seen);
    }
}
