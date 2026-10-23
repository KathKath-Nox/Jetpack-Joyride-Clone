package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

class IntroWallTest {
    private final GameSpeed speed = new GameSpeed(100);
    private final IntroWall wall = new IntroWall(500, speed);
    private final Hitbox onTheWall = new Hitbox(520, 300, 40, 60);
    private final Hitbox farAway = new Hitbox(100, 300, 40, 60);

    @Test
    void startsStanding() {
        assertTrue(wall.isStanding());
        assertFalse(wall.isFinished());
        assertEquals(0, wall.fragmentCount());
    }

    @Test
    void approachesAtTheSpeedOfTheDecor() {
        wall.update(1);
        assertEquals(400, wall.getX(), 1e-9);
    }

    @Test
    void touchesAPlayerThatOverlapsIt() {
        assertTrue(wall.touches(onTheWall));
        assertFalse(wall.touches(farAway));
    }

    @Test
    void goesFromTheCeilingToTheFloor() {
        assertEquals(0, wall.getY(), 1e-9);
        assertEquals(App.HEIGHT - Decor.FLOOR_HEIGHT, wall.getHitbox().getHeight(), 1e-9);
        assertTrue(wall.touches(new Hitbox(520, 0, 40, 60)));
    }

    @Test
    void shatteringBreaksItIntoAGridOfFragments() {
        wall.shatter(new Random(1));
        assertFalse(wall.isStanding());
        assertEquals(IntroWall.COLUMNS * IntroWall.ROWS, wall.fragmentCount());
    }

    @Test
    void aBrokenWallDoesNotBlockAnymore() {
        wall.shatter(new Random(1));
        assertFalse(wall.touches(onTheWall));
    }

    @Test
    void shatteringTwiceKeepsTheSameFragments() {
        wall.shatter(new Random(1));
        int count = wall.fragmentCount();
        wall.shatter(new Random(2));
        assertEquals(count, wall.fragmentCount());
    }

    @Test
    void fragmentsFlyThenTheEffectEnds() {
        wall.shatter(new Random(1));
        wall.update(0.1);
        assertFalse(wall.isFinished());
        for (int i = 0; i < 300 && !wall.isFinished(); i++) {
            wall.update(1.0 / 60);
        }
        assertTrue(wall.isFinished());
    }

    @Test
    void aWallLeftBehindIsFinished() {
        wall.update(10);
        assertTrue(wall.isFinished());
    }
}