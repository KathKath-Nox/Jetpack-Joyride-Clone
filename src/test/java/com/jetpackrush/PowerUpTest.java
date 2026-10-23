package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

class PowerUpTest {
    private final GameSpeed speed = new GameSpeed();

    @Test
    void scrollsLeftAtGameSpeed() {
        PowerUp powerUp = new PowerUp(100, 0, speed, new ShieldEffect());
        powerUp.update(0.5);
        assertEquals(100 - speed.getSpeed() * 0.5, powerUp.getX(), 1e-9);
    }

    @Test
    void isOffScreenOnceFullyPastTheLeftEdge() {
        assertTrue(new PowerUp(-1000, 0, speed, new ShieldEffect()).isOffScreen());
        assertFalse(new PowerUp(100, 0, speed, new ShieldEffect()).isOffScreen());
    }

    @Test
    void effectsHaveTheirOwnColor() {
        assertEquals(Color.CYAN, new ShieldEffect().getColor());
        assertEquals(Color.ORANGE, new VehicleEffect().getColor());
    }
}
