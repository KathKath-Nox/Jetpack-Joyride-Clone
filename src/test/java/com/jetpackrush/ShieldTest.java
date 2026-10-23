package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ShieldTest {
    private final Player player = new Player(200, 300, () -> false);
    private final PowerUp shield = new PowerUp(0, 0, new GameSpeed(), new ShieldEffect());

    @Test
    void hitWithoutShieldKills() {
        player.hit();
        assertTrue(player.isDead());
    }

    @Test
    void pickingUpShieldProtectsPlayer() {
        shield.onCollision(player);
        assertTrue(player.hasShield());
        assertTrue(shield.isCollected());
    }

    @Test
    void shieldAbsorbsOneHitThenDisappears() {
        shield.onCollision(player);
        player.hit();
        assertFalse(player.isDead());
        assertFalse(player.hasShield());
    }

    @Test
    void secondHitKills() {
        shield.onCollision(player);
        player.hit();
        player.hit();
        assertTrue(player.isDead());
    }

    @Test
    void collectedPowerUpCannotBeCollectedTwice() {
        shield.onCollision(player);
        player.hit();
        shield.onCollision(player);
        assertFalse(player.hasShield());
    }

    @Test
    void expireRemovesShield() {
        ShieldEffect effect = new ShieldEffect();
        effect.apply(player);
        effect.expire(player);
        assertFalse(player.hasShield());
    }
}
