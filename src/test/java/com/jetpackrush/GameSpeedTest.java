package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests de {@link GameSpeed}. */
class GameSpeedTest {

    @Test
    void startsAtDefaultSpeed() {
        assertEquals(GameSpeed.DEFAULT_SPEED, new GameSpeed().getSpeed());
    }

    @Test
    void setSpeedChangesTheValue() {
        GameSpeed speed = new GameSpeed();
        speed.setSpeed(450);
        assertEquals(450, speed.getSpeed());
    }

    @Test
    void negativeSpeedIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GameSpeed(-1));
        assertThrows(IllegalArgumentException.class, () -> new GameSpeed().setSpeed(-5));
    }
}
