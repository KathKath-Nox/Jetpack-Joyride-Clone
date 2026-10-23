package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MissileTest {
    private static final double SPEED = 100;

    private final GameSpeed speed = new GameSpeed(SPEED);
    private final Hitbox[] target = {new Hitbox(200, 300, 40, 60)};
    private final Player player = new Player(200, 300, () -> false);

    private Missile missile(double x) {
        return new Missile(x, 0, speed, () -> target[0]);
    }

    private static double aimedY(Hitbox box) {
        return box.getY() + box.getHeight() / 2 - Missile.HEIGHT / 2;
    }

    private static void fire(Missile missile) {
        missile.update(0.1);
        missile.update(Missile.WARNING_TIME);
    }

    @Test
    void startsWithAHarmlessWarning() {
        Missile m = missile(200);
        m.update(0.1);
        assertTrue(m.isWarning());
        assertFalse(m.collidesWith(player.getHitbox()));
    }

    @Test
    void warningDoesNotKillEvenIfOnCollisionIsCalled() {
        Missile m = missile(200);
        m.update(0.1);
        m.onCollision(player);
        assertFalse(player.isDead());
    }

    @Test
    void doesNotMoveHorizontallyDuringTheWarning() {
        Missile m = missile(900);
        m.update(0.5);
        assertEquals(900, m.getX(), 1e-9);
    }

    @Test
    void alertFollowsThePlayerBeforeTheLock() {
        Missile m = missile(900);
        m.update(0.1);
        assertEquals(aimedY(target[0]), m.getY(), 1e-9);
        target[0] = new Hitbox(200, 100, 40, 60);
        m.update(0.1);
        assertEquals(aimedY(target[0]), m.getY(), 1e-9);
    }

    @Test
    void heightIsLockedBeforeFiring() {
        Missile m = missile(900);
        m.update(0.1);
        m.update(Missile.WARNING_TIME - Missile.LOCK_TIME);
        double locked = m.getY();
        target[0] = new Hitbox(200, 500, 40, 60);
        m.update(0.1);
        assertEquals(locked, m.getY(), 1e-9);
    }

    @Test
    void firesAfterTheWarningAndFliesFasterThanTheScroll() {
        Missile m = missile(900);
        fire(m);
        assertTrue(m.isFlying());
        double before = m.getX();
        m.update(0.5);
        assertEquals(before - (SPEED + Missile.EXTRA_SPEED) * 0.5, m.getX(), 1e-9);
    }

    @Test
    void flightKeepsTheLockedHeight() {
        Missile m = missile(900);
        fire(m);
        double locked = m.getY();
        target[0] = new Hitbox(200, 100, 40, 60);
        m.update(0.5);
        assertEquals(locked, m.getY(), 1e-9);
    }

    @Test
    void killsThePlayerWhenFlyingThroughHim() {
        Missile m = missile(200);
        fire(m);
        assertTrue(m.collidesWith(player.getHitbox()));
        m.onCollision(player);
        assertTrue(player.isDead());
    }

    @Test
    void shieldAbsorbsOnlyOneMissile() {
        player.giveShield();
        Missile m = missile(200);
        fire(m);
        m.onCollision(player);
        m.onCollision(player);
        assertFalse(player.isDead());
        assertFalse(player.hasShield());
    }

    @Test
    void resetRestartsTheWarning() {
        Missile m = missile(900);
        fire(m);
        m.reset(1300, 0);
        assertTrue(m.isWarning());
        assertEquals(1300, m.getX(), 1e-9);
    }

    @Test
    void goesOffScreenAfterCrossingTheWindow() {
        Missile m = missile(900);
        fire(m);
        assertFalse(m.isOffScreen());
        m.update(10);
        assertTrue(m.isOffScreen());
    }

    @Test
    void withoutTargetTheMissileKeepsItsHeight() {
        Missile m = new Missile(900, 123, speed, () -> null);
        m.update(0.1);
        assertEquals(123, m.getY(), 1e-9);
    }

    @Test
    void aimStaysAboveTheFloor() {
        target[0] = new Hitbox(200, 650, 40, 60);
        Missile m = missile(900);
        m.update(0.1);
        assertTrue(m.getY() + Missile.HEIGHT <= App.HEIGHT - Decor.FLOOR_HEIGHT);
    }

    @Test
    void typeIsMissile() {
        assertEquals(ObstacleType.MISSILE, missile(0).getType());
    }
}
