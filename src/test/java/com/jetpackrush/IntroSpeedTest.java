package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IntroSpeedTest {

    @Test
    void startsWithABurst() {
        assertEquals(PlayState.BURST_SPEED, PlayState.introSpeed(0), 1e-9);
        assertTrue(PlayState.introSpeed(0) > PlayState.WALK_SPEED);
    }

    @Test
    void slowsDownToWalkingSpeed() {
        assertEquals(PlayState.WALK_SPEED, PlayState.introSpeed(PlayState.BURST_TIME), 1e-9);
    }

    @Test
    void staysAtWalkingSpeedAfterwards() {
        assertEquals(PlayState.WALK_SPEED, PlayState.introSpeed(PlayState.BURST_TIME * 5), 1e-9);
    }

    @Test
    void neverSpeedsUpAgain() {
        double previous = PlayState.introSpeed(0);
        for (int i = 1; i <= 20; i++) {
            double current = PlayState.introSpeed(i * PlayState.BURST_TIME / 20);
            assertTrue(current <= previous);
            previous = current;
        }
    }

    @Test
    void negativeTimeIsTreatedAsTheStart() {
        assertEquals(PlayState.introSpeed(0), PlayState.introSpeed(-1), 1e-9);
    }
}