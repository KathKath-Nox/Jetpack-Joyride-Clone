package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChunkTest {
    private static final double H = Chunk.PLAY_BOTTOM;

    @Test
    void emptyChunkIsPassable() {
        assertTrue(new Chunk("vide", 100, List.of()).isPassable());
    }

    @Test
    void wallFromCeilingToFloorIsNotPassable() {
        Chunk wall = new Chunk("mur", 50, List.of(new Chunk.Rect(0, 0, 50, H)));
        assertFalse(wall.isPassable());
    }

    @Test
    void gapSmallerThanThePlayerIsNotPassable() {
        Chunk tight = new Chunk("etroit", 200, List.of(
            new Chunk.Rect(0, 0, 200, 300),
            new Chunk.Rect(0, 300 + Chunk.MIN_GAP - 1, 200, H)));
        assertFalse(tight.isPassable());
    }

    @Test
    void gapBigEnoughIsPassable() {
        Chunk corridor = new Chunk("couloir", 200, List.of(
            new Chunk.Rect(0, 0, 200, 300),
            new Chunk.Rect(0, 300 + Chunk.MIN_GAP, 200, H)));
        assertTrue(corridor.isPassable());
    }

    @Test
    void gapsThatDoNotLineUpAreNotPassable() {
        // Passage en haut dans la première moitié, en bas dans la seconde : aucune jonction
        Chunk shifted = new Chunk("decale", 200, List.of(
            new Chunk.Rect(0, 150, 100, H - 150),
            new Chunk.Rect(100, 0, 100, H - 150)));
        assertFalse(shifted.isPassable());
    }

    @Test
    void rectangleOutsideTheChunkIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new Chunk("debord", 50, List.of(new Chunk.Rect(10, 0, 50, 50))));
    }

    @Test
    void spawnPlacesObstaclesFromTheOrigin() {
        Chunk chunk = new Chunk("c", 100, List.of(new Chunk.Rect(20, 100, 30, 40)));
        List<Obstacle> obstacles = chunk.spawn(500, new GameSpeed());
        assertEquals(1, obstacles.size());
        assertEquals(520, obstacles.get(0).getX(), 1e-9);
        assertEquals(100, obstacles.get(0).getY(), 1e-9);
        assertEquals(30, obstacles.get(0).getHitbox().getWidth(), 1e-9);
    }
}
