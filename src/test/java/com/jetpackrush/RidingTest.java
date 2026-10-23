package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RidingTest {
    @Test
    void timeLeftCountsDown() {
        Player player = new Player(200, 300, () -> false);
        new VehicleEffect().apply(player);
        for (int i = 0; i < 60; i++) {
            player.update(1.0 / 60);
        }
        Riding riding = (Riding) player.getState();
        assertEquals(Riding.DURATION - 1.0, riding.getTimeLeft(), 1e-6);
    }
}
