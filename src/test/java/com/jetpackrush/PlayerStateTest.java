package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerStateTest {
    private static final double DT = 1.0 / 60;

    private final boolean[] thrust = {false};
    private final Player player = new Player(200, 300, () -> thrust[0]);

    private void run(int frames) {
        for (int i = 0; i < frames; i++) {
            player.update(DT);
        }
    }

    @Test
    void startsFalling() {
        assertTrue(player.getState() instanceof Falling);
    }

    @Test
    void thrustSwitchesToFlying() {
        thrust[0] = true;
        run(1);
        assertTrue(player.getState() instanceof Flying);
    }

    @Test
    void releaseSwitchesBackToFalling() {
        thrust[0] = true;
        run(5);
        thrust[0] = false;
        run(1);
        assertTrue(player.getState() instanceof Falling);
    }

    @Test
    void dieSwitchesToDead() {
        player.die();
        assertTrue(player.isDead());
        assertTrue(player.getState() instanceof Dead);
    }

    @Test
    void deadIgnoresThrustAndStaysDead() {
        player.die();
        thrust[0] = true;
        run(600);
        assertTrue(player.isDead());
        double ground = App.HEIGHT - Player.GROUND_HEIGHT - player.getHitbox().getHeight();
        assertEquals(ground, player.getY(), 1e-9);
    }

    @Test
    void aliveByDefault() {
        assertFalse(player.isDead());
    }
}
