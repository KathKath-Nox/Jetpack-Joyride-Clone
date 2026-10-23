package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CoinTest {
    private final GameSpeed speed = new GameSpeed();
    private final ScoreManager score = new ScoreManager();
    private final Player player = new Player(0, 0, new KeyboardInput());

    @Test
    void scrollsLeftAtGameSpeed() {
        Coin coin = new Coin(100, 0, speed, score);
        coin.update(0.5);
        assertEquals(100 - speed.getSpeed() * 0.5, coin.getX(), 1e-9);
    }

    @Test
    void collisionCreditsOneCoinOnlyOnce() {
        Coin coin = new Coin(0, 0, speed, score);
        coin.onCollision(player);
        coin.onCollision(player);
        assertTrue(coin.isCollected());
        assertEquals(1, score.getCoins());
    }

    @Test
    void isOffScreenOncePastTheLeftEdge() {
        assertTrue(new Coin(-1000, 0, speed, score).isOffScreen());
        assertFalse(new Coin(100, 0, speed, score).isOffScreen());
    }
}
