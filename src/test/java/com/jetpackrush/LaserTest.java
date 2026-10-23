package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LaserTest {
    private final GameSpeed speed = new GameSpeed(100);
    private final Player player = new Player(200, 300, () -> false);

    private Laser laser(double y) {
        return new Laser(y, speed);
    }

    private Laser activeLaser(double y) {
        Laser l = laser(y);
        l.update(Laser.WARNING_TIME);
        return l;
    }

    @Test
    void startsWithAHarmlessWarning() {
        Laser l = laser(300);
        l.update(0.1);
        assertTrue(l.isWarning());
        assertFalse(l.collidesWith(player.getHitbox()));
    }

    @Test
    void warningDoesNotKillEvenIfOnCollisionIsCalled() {
        Laser l = laser(300);
        l.update(0.1);
        l.onCollision(player);
        assertFalse(player.isDead());
    }

    @Test
    void becomesActiveAfterTheWarning() {
        assertTrue(activeLaser(300).isActive());
    }

    @Test
    void killsThePlayerWhileActive() {
        Laser l = activeLaser(300);
        assertTrue(l.collidesWith(player.getHitbox()));
        l.onCollision(player);
        assertTrue(player.isDead());
    }

    @Test
    void onlyHitsWhatIsAtItsHeight() {
        Laser l = activeLaser(100);
        assertFalse(l.collidesWith(player.getHitbox()));
    }

    @Test
    void spansTheWholeWidthFromTheLeftEdge() {
        Hitbox box = laser(300).getHitbox();
        assertEquals(0, box.getX(), 1e-9);
        assertEquals(App.WIDTH, box.getWidth(), 1e-9);
    }

    @Test
    void doesNotScrollWithTheDecor() {
        Laser l = laser(300);
        l.update(0.5);
        assertEquals(0, l.getX(), 1e-9);
        assertEquals(300, l.getY(), 1e-9);
    }

    @Test
    void isNotFinishedBeforeTheEndOfTheActivePhase() {
        Laser l = activeLaser(300);
        assertFalse(l.isOffScreen());
        assertFalse(l.isDone());
    }

    @Test
    void finishesAfterTheActivePhaseAndCanBeReleased() {
        Laser l = activeLaser(300);
        l.update(Laser.ACTIVE_TIME);
        assertTrue(l.isDone());
        assertTrue(l.isOffScreen());
        assertFalse(l.collidesWith(player.getHitbox()));
    }

    @Test
    void aBigStepGoesThroughBothPhases() {
        Laser l = laser(300);
        l.update(10);
        assertTrue(l.isDone());
    }

    @Test
    void shieldAbsorbsOnlyOneActivation() {
        player.giveShield();
        Laser l = activeLaser(300);
        l.onCollision(player);
        l.onCollision(player);
        assertFalse(player.isDead());
        assertFalse(player.hasShield());
    }

    @Test
    void resetRestartsTheWarningAtTheNewHeight() {
        Laser l = activeLaser(300);
        l.update(Laser.ACTIVE_TIME);
        l.reset(1330, 120);
        assertTrue(l.isWarning());
        assertFalse(l.isOffScreen());
        assertEquals(0, l.getX(), 1e-9);
        assertEquals(120, l.getY(), 1e-9);
    }

    @Test
    void resetMakesTheLaserLethalAgain() {
        player.giveShield();
        Laser l = activeLaser(300);
        l.onCollision(player);
        l.reset(0, 300);
        l.update(Laser.WARNING_TIME);
        l.onCollision(player);
        assertTrue(player.isDead());
    }

    @Test
    void typeIsLaser() {
        assertEquals(ObstacleType.LASER, laser(0).getType());
    }
}
