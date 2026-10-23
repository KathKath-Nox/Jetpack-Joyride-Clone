package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CrashEffectTest {

    private Player player() {
        return new Player(200, 300, () -> false);
    }

    @Test
    void destroyedVehicleThrowsDebris() {
        Player player = player();
        player.setState(new Riding());
        player.hit();
        assertTrue(player.effectCount() > 0);
    }

    @Test
    void deathThrowsDebris() {
        Player player = player();
        player.die();
        assertTrue(player.effectCount() > 0);
    }

    @Test
    void dyingTwiceDoesNotRepeatTheExplosion() {
        Player player = player();
        player.die();
        int count = player.effectCount();
        player.die();
        assertEquals(count, player.effectCount());
    }

    @Test
    void shieldBreakMakesNoDebris() {
        Player player = player();
        player.giveShield();
        player.hit();
        assertEquals(0, player.effectCount());
    }

    @Test
    void effectsDisappearOnceAliveAgain() {
        Player player = player();
        player.die();
        for (int i = 0; i < 300; i++) {
            player.update(1.0 / 60);
        }
        assertTrue(player.isDead());
        assertTrue(player.effectCount() < 60);
    }
}
