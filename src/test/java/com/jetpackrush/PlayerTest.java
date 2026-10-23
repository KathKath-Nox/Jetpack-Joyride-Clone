package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerTest {
    private static final double DT = 1.0 / 60;

    private final boolean[] thrust = {false};
    private final Player player = new Player(200, 300, () -> thrust[0]);

    private void run(int frames) {
        for (int i = 0; i < frames; i++) {
            player.update(DT);
        }
    }

    @Test
    void fallsWithoutThrust() {
        run(30);
        assertTrue(player.getY() > 300);
    }

    @Test
    void risesWithThrust() {
        thrust[0] = true;
        run(30);
        assertTrue(player.getY() < 300);
    }

    @Test
    void keepsRisingBrieflyAfterRelease() {
        thrust[0] = true;
        run(30);
        double before = player.getY();
        thrust[0] = false;
        run(1);
        assertTrue(player.getY() < before);
    }

    @Test
    void stopsAtCeilingWithoutDying() {
        thrust[0] = true;
        run(600);
        assertEquals(0, player.getY(), 1e-9);
        assertEquals(0, player.getVelocityY(), 1e-9);
    }

    @Test
    void stopsOnGround() {
        run(600);
        double expected = App.HEIGHT - Player.GROUND_HEIGHT - player.getHitbox().getHeight();
        assertEquals(expected, player.getY(), 1e-9);
        assertEquals(0, player.getVelocityY(), 1e-9);
    }
}
