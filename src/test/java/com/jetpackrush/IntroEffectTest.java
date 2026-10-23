package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IntroEffectTest {

    @Test
    void enteringThrowsEffects() {
        Player player = new Player(200, 300, () -> false);
        player.enter();
        assertTrue(player.effectCount() > 0);
    }

    @Test
    void introEffectsEndByThemselves() {
        Player player = new Player(200, 300, () -> false);
        player.enter();
        for (int i = 0; i < 300; i++) {
            player.update(1.0 / 60);
        }
        assertTrue(player.effectCount() == 0);
    }
}
