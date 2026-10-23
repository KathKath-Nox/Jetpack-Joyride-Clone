package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ObstaclePoolTest {
    private final ObstaclePool pool = new ObstaclePool(new ObstacleFactory(new GameSpeed(100)));

    @Test
    void createsANewObstacleWhenThePoolIsEmpty() {
        Obstacle obstacle = pool.acquire(ObstacleType.ZAPPER_HORIZONTAL, 5, 6);
        assertEquals(ObstacleType.ZAPPER_HORIZONTAL, obstacle.getType());
        assertEquals(5, obstacle.getX(), 1e-9);
    }

    @Test
    void reusesAReleasedObstacleAtTheNewPosition() {
        Obstacle first = pool.acquire(ObstacleType.ZAPPER_VERTICAL, 0, 0);
        pool.release(first);
        Obstacle second = pool.acquire(ObstacleType.ZAPPER_VERTICAL, 300, 40);
        assertSame(first, second);
        assertEquals(300, second.getX(), 1e-9);
        assertEquals(40, second.getY(), 1e-9);
    }

    @Test
    void everyTypeCanBeReused() {
        for (ObstacleType type : ObstacleType.values()) {
            Obstacle first = pool.acquire(type, 100, 100);
            pool.release(first);
            Obstacle second = pool.acquire(type, 200, 100);
            assertSame(first, second);
            assertEquals(type, second.getType());
        }
    }

    @Test
    void doesNotMixTypes() {
        Obstacle horizontal = pool.acquire(ObstacleType.ZAPPER_HORIZONTAL, 0, 0);
        pool.release(horizontal);
        Obstacle vertical = pool.acquire(ObstacleType.ZAPPER_VERTICAL, 0, 0);
        assertNotSame(horizontal, vertical);
        assertEquals(1, pool.freeCount(ObstacleType.ZAPPER_HORIZONTAL));
    }

    @Test
    void releasingTheSameObstacleTwiceStoresItOnce() {
        Obstacle obstacle = pool.acquire(ObstacleType.ZAPPER_VERTICAL, 0, 0);
        pool.release(obstacle);
        pool.release(obstacle);
        assertEquals(1, pool.freeCount(ObstacleType.ZAPPER_VERTICAL));
        Obstacle a = pool.acquire(ObstacleType.ZAPPER_VERTICAL, 0, 0);
        Obstacle b = pool.acquire(ObstacleType.ZAPPER_VERTICAL, 0, 0);
        assertNotSame(a, b);
    }

    @Test
    void reusedObstacleIsLethalAgain() {
        Player player = new Player(200, 300, () -> false);
        player.giveShield();
        Obstacle obstacle = pool.acquire(ObstacleType.ZAPPER_HORIZONTAL, 0, 0);
        obstacle.onCollision(player);
        pool.release(obstacle);
        pool.acquire(ObstacleType.ZAPPER_HORIZONTAL, 0, 0).onCollision(player);
        assertTrue(player.isDead());
    }

    @Test
    void reusedMissileRestartsItsWarning() {
        Missile missile = (Missile) pool.acquire(ObstacleType.MISSILE, 900, 100);
        missile.update(0.1);
        missile.update(Missile.WARNING_TIME);
        assertTrue(missile.isFlying());
        pool.release(missile);
        Missile again = (Missile) pool.acquire(ObstacleType.MISSILE, 900, 200);
        assertSame(missile, again);
        assertTrue(again.isWarning());
    }

    @Test
    void reusedLaserRestartsItsWarningAtTheNewHeight() {
        Laser laser = (Laser) pool.acquire(ObstacleType.LASER, 0, 100);
        laser.update(Laser.WARNING_TIME);
        laser.update(Laser.ACTIVE_TIME);
        assertTrue(laser.isOffScreen());
        pool.release(laser);
        Laser again = (Laser) pool.acquire(ObstacleType.LASER, 0, 200);
        assertSame(laser, again);
        assertFalse(again.isOffScreen());
        assertTrue(again.isWarning());
        assertEquals(200, again.getY(), 1e-9);
    }

    @Test
    void reusedRotatingZapperStopsSpinning() {
        Zapper zapper = (Zapper) pool.acquire(ObstacleType.ZAPPER_ROTATING, 0, 0);
        zapper.update(1);
        assertTrue(zapper.getSpin() > 0);
        pool.release(zapper);
        Zapper again = (Zapper) pool.acquire(ObstacleType.ZAPPER_ROTATING, 0, 0);
        assertEquals(0, again.getSpin(), 1e-9);
    }

    @Test
    void poolDoesNotGrowWhenObstaclesAreReused() {
        for (int i = 0; i < 100; i++) {
            pool.release(pool.acquire(ObstacleType.ZAPPER_DIAGONAL, 0, 0));
        }
        assertEquals(1, pool.freeCount(ObstacleType.ZAPPER_DIAGONAL));
    }
}