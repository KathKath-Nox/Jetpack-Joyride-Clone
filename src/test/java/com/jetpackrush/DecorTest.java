package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DecorTest {

    @Test
    void offsetAdvancesWithSpeed() {
        Decor decor = new Decor(new GameSpeed(100));
        decor.update(0.5);
        assertEquals(50, decor.getOffset(), 1e-9);
    }

    @Test
    void offsetWrapsAroundThePattern() {
        Decor decor = new Decor(new GameSpeed(100));
        decor.update(3.5);
        assertEquals(30, decor.getOffset(), 1e-9);
    }

    @Test
    void offsetFollowsSpeedChanges() {
        GameSpeed speed = new GameSpeed(100);
        Decor decor = new Decor(speed);
        decor.update(1);
        speed.setSpeed(200);
        decor.update(0.5);
        assertEquals(200, decor.getOffset(), 1e-9);
    }

    @Test
    void layoutIsConsistent() {
        assertEquals(Decor.ENTRY_X + Decor.ROOM_WIDTH, Decor.ROOM_END, 1e-9);
        assertEquals(Decor.ROOM_END - Decor.ROOM_FADE, Decor.CORRIDOR_START, 1e-9);
    }

    @Test
    void startsAtZeroDistance() {
        Decor decor = new Decor(new GameSpeed(100));
        assertEquals(0, decor.getDistance(), 1e-9);
        assertTrue(decor.isInRoom());
        assertEquals(0, decor.getZone());
    }

    @Test
    void noMovementAtZeroSpeed() {
        Decor decor = new Decor(new GameSpeed(0));
        decor.update(1);
        assertEquals(0, decor.getOffset(), 1e-9);
        assertEquals(0, decor.getDistance(), 1e-9);
    }

    @Test
    void distanceAdvancesWithSpeed() {
        Decor decor = new Decor(new GameSpeed(100));
        decor.update(2);
        assertEquals(200, decor.getDistance(), 1e-9);
    }

    @Test
    void leavesTheRoomOnceItsEndIsScrolledAway() {
        Decor decor = new Decor(new GameSpeed(Decor.ROOM_END));
        decor.update(0.5);
        assertTrue(decor.isInRoom());
        decor.update(0.5);
        assertFalse(decor.isInRoom());
    }

    @Test
    void zonesCountAfterTheRoomExit() {
        Decor decor = new Decor(new GameSpeed(1));
        decor.update(Decor.CORRIDOR_START + Decor.ZONE_LENGTH - 1);
        assertEquals(0, decor.getZone());
        decor.update(1);
        assertEquals(1, decor.getZone());
        decor.update(Decor.ZONE_LENGTH);
        assertEquals(2, decor.getZone());
    }

    @Test
    void resetGoesBackToTheStart() {
        Decor decor = new Decor(new GameSpeed(Decor.ROOM_END));
        decor.update(10);
        decor.reset();
        assertEquals(0, decor.getDistance(), 1e-9);
        assertEquals(0, decor.getOffset(), 1e-9);
        assertEquals(0, decor.getZone());
        assertTrue(decor.isInRoom());
    }

    @Test
    void backgroundsFollowEachOtherThenLoop() {
        assertEquals(Decor.Kind.METAL, Decor.kindOf(0));
        assertEquals(Decor.Kind.LAB, Decor.kindOf(1));
        assertEquals(Decor.Kind.SWAMP, Decor.kindOf(2));
        assertEquals(Decor.Kind.JUNKYARD, Decor.kindOf(3));
        assertEquals(Decor.Kind.METAL, Decor.kindOf(4));
        assertEquals(Decor.Kind.SWAMP, Decor.kindOf(6));
    }

    @Test
    void blendGoesSmoothlyFromZeroToOne() {
        assertEquals(0, Decor.blend(0), 1e-9);
        assertEquals(0.5, Decor.blend(0.5), 1e-9);
        assertEquals(1, Decor.blend(1), 1e-9);
        double previous = 0;
        for (int i = 1; i <= 20; i++) {
            double current = Decor.blend(i / 20.0);
            assertTrue(current >= previous);
            previous = current;
        }
    }

    @Test
    void blendIsClampedOutsideTheRange() {
        assertEquals(0, Decor.blend(-3), 1e-9);
        assertEquals(1, Decor.blend(7), 1e-9);
    }

    @Test
    void aBackgroundLastsAboutAMinuteAtCruisingSpeed() {
        double seconds = Decor.ZONE_LENGTH / 520.0;
        assertTrue(seconds >= 40 && seconds <= 70);
    }
}