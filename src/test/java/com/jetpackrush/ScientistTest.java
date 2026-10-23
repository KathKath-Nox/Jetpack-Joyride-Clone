package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScientistTest {
    private static final double REST_Y = App.HEIGHT - Decor.FLOOR_HEIGHT - Scientist.HEIGHT;

    private final GameSpeed speed = new GameSpeed(100);
    private final Scientist scientist = new Scientist(500, 30, 0, speed);

    private void fall(Scientist s) {
        for (int i = 0; i < 300 && !s.isFainted(); i++) {
            s.update(1.0 / 60);
        }
    }

    @Test
    void startsWalkingOnTheFloor() {
        assertTrue(scientist.isWalking());
        assertEquals(REST_Y, scientist.getY(), 1e-9);
    }

    @Test
    void walksAgainstTheScroll() {
        scientist.update(1);
        assertEquals(430, scientist.getX(), 1e-9);
    }

    @Test
    void ignoresAPlayerFarAway() {
        scientist.react(new Hitbox(100, 300, 40, 60), true);
        assertTrue(scientist.isWalking());
    }

    @Test
    void touchingThePlayerKnocksItDown() {
        scientist.react(new Hitbox(510, 560, 40, 60), false);
        assertTrue(scientist.isKnocked());
    }

    @Test
    void theJetpackFlameKnocksItDownWhileThrusting() {
        Hitbox above = new Hitbox(500, 480, 40, 60);
        scientist.react(above, true);
        assertTrue(scientist.isKnocked());
    }

    @Test
    void withoutThrustThePlayerAboveOnlyScaresIt() {
        Hitbox above = new Hitbox(500, 480, 40, 60);
        scientist.react(above, false);
        assertFalse(scientist.isKnocked());
        assertTrue(scientist.isScared());
    }

    @Test
    void calmsDownWhenThePlayerIsGone() {
        scientist.react(new Hitbox(500, 480, 40, 60), false);
        scientist.react(new Hitbox(100, 300, 40, 60), false);
        assertTrue(scientist.isWalking());
    }

    @Test
    void fliesUpThenLandsFainted() {
        scientist.react(new Hitbox(510, 560, 40, 60), false);
        scientist.update(0.05);
        assertTrue(scientist.getY() < REST_Y);
        assertFalse(scientist.isFainted());
        fall(scientist);
        assertTrue(scientist.isFainted());
        assertEquals(REST_Y, scientist.getY(), 1e-9);
    }

    @Test
    void aBigStepStillLands() {
        scientist.react(new Hitbox(510, 560, 40, 60), false);
        scientist.update(10);
        assertTrue(scientist.isFainted());
        assertEquals(REST_Y, scientist.getY(), 1e-9);
    }

    @Test
    void isPushedAwayFromThePlayer() {
        Scientist still = new Scientist(500, 0, 0, new GameSpeed(0));
        still.react(new Hitbox(480, 560, 40, 60), false);
        double before = still.getX();
        still.update(0.1);
        assertTrue(still.getX() > before);
    }

    @Test
    void faintedScientistScrollsWithTheDecor() {
        scientist.react(new Hitbox(510, 560, 40, 60), false);
        fall(scientist);
        double x = scientist.getX();
        scientist.update(1);
        assertEquals(x - 100, scientist.getX(), 1e-9);
        assertEquals(REST_Y, scientist.getY(), 1e-9);
    }

    @Test
    void cannotBeKnockedTwice() {
        scientist.react(new Hitbox(510, 560, 40, 60), false);
        fall(scientist);
        scientist.react(new Hitbox(100, 300, 40, 60), false);
        assertTrue(scientist.isFainted());
    }

    @Test
    void isOffScreenOnceFullyOnTheLeft() {
        assertFalse(scientist.isOffScreen());
        scientist.update(10);
        assertTrue(scientist.isOffScreen());
    }
}