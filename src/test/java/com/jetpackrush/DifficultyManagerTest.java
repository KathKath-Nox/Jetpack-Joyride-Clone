package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests de {@link DifficultyManager}. */
class DifficultyManagerTest {

    @Test
    void startsAtStartSpeed() {
        assertEquals(DifficultyManager.START_SPEED, DifficultyManager.speedAt(0), 1e-9);
    }

    @Test
    void gameStartsAtTheSpeedTheIntroEndsWith() {
        assertEquals(PlayState.WALK_SPEED, DifficultyManager.START_SPEED, 1e-9);
    }

    @Test
    void staysFlatDuringAStep() {
        assertEquals(DifficultyManager.START_SPEED,
            DifficultyManager.speedAt(DifficultyManager.STEP_TIME - 0.01), 1e-9);
    }

    @Test
    void goesUpOneStepEachInterval() {
        double step = DifficultyManager.STEP_TIME;
        assertEquals(DifficultyManager.START_SPEED + DifficultyManager.STEP_GAIN,
            DifficultyManager.speedAt(step), 1e-9);
        assertEquals(DifficultyManager.START_SPEED + 2 * DifficultyManager.STEP_GAIN,
            DifficultyManager.speedAt(2 * step + 0.5), 1e-9);
    }

    @Test
    void stopsAtTheCap() {
        assertEquals(DifficultyManager.MAX_SPEED, DifficultyManager.speedAt(10_000), 1e-9);
    }

    @Test
    void neverSlowsDown() {
        double previous = DifficultyManager.speedAt(0);
        for (int i = 1; i <= 200; i++) {
            double current = DifficultyManager.speedAt(i * 0.5);
            assertTrue(current >= previous);
            assertTrue(current <= DifficultyManager.MAX_SPEED);
            previous = current;
        }
    }

    @Test
    void negativeTimeIsTreatedAsTheStart() {
        assertEquals(DifficultyManager.speedAt(0), DifficultyManager.speedAt(-3), 1e-9);
    }

    @Test
    void updateWritesTheCurrentStepIntoGameSpeed() {
        GameSpeed speed = new GameSpeed(PlayState.BURST_SPEED);
        DifficultyManager difficulty = new DifficultyManager(speed);

        difficulty.update(0.1);
        assertEquals(DifficultyManager.START_SPEED, speed.getSpeed(), 1e-9);

        for (int i = 0; i < 150; i++) {
            difficulty.update(0.1);
        }
        assertEquals(DifficultyManager.START_SPEED + DifficultyManager.STEP_GAIN, speed.getSpeed(), 1e-9);
    }
}
