package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RidingHoldTest {
    private static final double DT = 1.0 / 60;

    private boolean pressed;

    private Player groundedRider() {
        pressed = false;
        Player player = new Player(200, 300, () -> pressed);
        player.setState(new Riding());
        for (int i = 0; i < 120; i++) {
            player.update(DT);
        }
        return player;
    }

    @Test
    void shortPressHopsWithoutReachingTheTop() {
        Player player = groundedRider();
        double highest = player.getHitbox().getY();
        for (int i = 0; i < 120; i++) {
            pressed = i < 6;
            player.update(DT);
            highest = Math.min(highest, player.getHitbox().getY());
        }
        assertTrue(highest < 500);
        assertTrue(highest > 200);
    }

    @Test
    void holdingFliesToTheTop() {
        Player player = groundedRider();
        pressed = true;
        for (int i = 0; i < 120; i++) {
            player.update(DT);
        }
        assertEquals(0, player.getHitbox().getY(), 1e-6);
        assertTrue(((Riding) player.getState()).isThrusting());
    }

    @Test
    void releasingAfterAHoldFallsBackToTheGround() {
        Player player = groundedRider();
        for (int i = 0; i < 400; i++) {
            pressed = i < 90;
            player.update(DT);
        }
        assertTrue(player.isOnGround());
        assertFalse(((Riding) player.getState()).isThrusting());
    }
}
