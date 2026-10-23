package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ZapperTest {
    private final GameSpeed speed = new GameSpeed(100);
    private final Player player = new Player(200, 300, () -> false);

    private Zapper zapper(Zapper.Orientation orientation, double x, double y) {
        return new Zapper(orientation, x, y, speed);
    }

    private Zapper still(Zapper.Orientation orientation) {
        return new Zapper(orientation, 0, 0, new GameSpeed(0));
    }

    @Test
    void horizontalZapperIsWiderThanTall() {
        Hitbox box = zapper(Zapper.Orientation.HORIZONTAL, 0, 0).getHitbox();
        assertTrue(box.getWidth() > box.getHeight());
    }

    @Test
    void verticalZapperIsTallerThanWide() {
        Hitbox box = zapper(Zapper.Orientation.VERTICAL, 0, 0).getHitbox();
        assertTrue(box.getHeight() > box.getWidth());
    }

    @Test
    void movesLeftAtGameSpeed() {
        Zapper z = zapper(Zapper.Orientation.HORIZONTAL, 500, 100);
        z.update(0.5);
        assertEquals(450, z.getX(), 1e-9);
        assertEquals(100, z.getY(), 1e-9);
    }

    @Test
    void isOffScreenOnceFullyOnTheLeft() {
        Zapper z = zapper(Zapper.Orientation.VERTICAL, 0, 0);
        assertFalse(z.isOffScreen());
        z.reset(-z.getHitbox().getWidth() - 1, 0);
        assertTrue(z.isOffScreen());
    }

    @Test
    void killsThePlayerOnCollision() {
        zapper(Zapper.Orientation.HORIZONTAL, 0, 0).onCollision(player);
        assertTrue(player.isDead());
    }

    @Test
    void shieldAbsorbsOnlyOneHitEvenIfTheOverlapLasts() {
        player.giveShield();
        Zapper z = zapper(Zapper.Orientation.HORIZONTAL, 0, 0);
        z.onCollision(player);
        z.onCollision(player);
        assertFalse(player.isDead());
        assertFalse(player.hasShield());
    }

    @Test
    void resetMakesTheZapperLethalAgain() {
        player.giveShield();
        Zapper z = zapper(Zapper.Orientation.HORIZONTAL, 0, 0);
        z.onCollision(player);
        z.reset(500, 100);
        z.onCollision(player);
        assertTrue(player.isDead());
    }

    @Test
    void horizontalUsesItsWholeBox() {
        Zapper z = zapper(Zapper.Orientation.HORIZONTAL, 0, 0);
        assertTrue(z.collidesWith(new Hitbox(80, 5, 10, 10)));
    }

    @Test
    void diagonalIgnoresTheEmptyCornersOfItsBox() {
        Zapper z = zapper(Zapper.Orientation.DIAGONAL, 0, 0);
        Hitbox corner = new Hitbox(100, 2, 10, 10);
        assertTrue(z.getHitbox().intersects(corner));
        assertFalse(z.collidesWith(corner));
    }

    @Test
    void diagonalHitsOnItsLine() {
        Zapper z = zapper(Zapper.Orientation.DIAGONAL, 0, 0);
        assertTrue(z.collidesWith(new Hitbox(55, 55, 10, 10)));
    }

    @Test
    void longHorizontalIsMuchLongerThanTheNormalOne() {
        double normal = zapper(Zapper.Orientation.HORIZONTAL, 0, 0).getHitbox().getWidth();
        double longer = zapper(Zapper.Orientation.HORIZONTAL_LONG, 0, 0).getHitbox().getWidth();
        assertTrue(longer > 2 * normal);
    }

    @Test
    void longVerticalIsTallerThanTheNormalOne() {
        double normal = zapper(Zapper.Orientation.VERTICAL, 0, 0).getHitbox().getHeight();
        double longer = zapper(Zapper.Orientation.VERTICAL_LONG, 0, 0).getHitbox().getHeight();
        assertTrue(longer > normal);
    }

    @Test
    void longHorizontalKillsAlongItsWholeLength() {
        Zapper z = zapper(Zapper.Orientation.HORIZONTAL_LONG, 0, 0);
        assertTrue(z.collidesWith(new Hitbox(10, 5, 10, 10)));
        assertTrue(z.collidesWith(new Hitbox(500, 5, 10, 10)));
    }

    @Test
    void longZappersHaveTheirOwnTypes() {
        assertEquals(ObstacleType.ZAPPER_HORIZONTAL_LONG,
            zapper(Zapper.Orientation.HORIZONTAL_LONG, 0, 0).getType());
        assertEquals(ObstacleType.ZAPPER_VERTICAL_LONG,
            zapper(Zapper.Orientation.VERTICAL_LONG, 0, 0).getType());
    }

    @Test
    void rotatingZapperHasItsOwnType() {
        assertEquals(ObstacleType.ZAPPER_ROTATING, zapper(Zapper.Orientation.ROTATING, 0, 0).getType());
    }

    @Test
    void rotatingZapperSpins() {
        Zapper z = still(Zapper.Orientation.ROTATING);
        z.update(1);
        assertEquals(Zapper.SPIN_SPEED, z.getSpin(), 1e-9);
    }

    @Test
    void otherZappersDoNotSpin() {
        Zapper z = still(Zapper.Orientation.HORIZONTAL);
        z.update(1);
        assertEquals(0, z.getSpin(), 1e-9);
    }

    @Test
    void resetStopsTheSpin() {
        Zapper z = still(Zapper.Orientation.ROTATING);
        z.update(1);
        z.reset(0, 0);
        assertEquals(0, z.getSpin(), 1e-9);
    }

    @Test
    void rotatingBarStartsHorizontalThenFollowsTheRotation() {
        Zapper z = still(Zapper.Orientation.ROTATING);
        Hitbox right = new Hitbox(165, 95, 10, 10);
        Hitbox top = new Hitbox(95, 25, 10, 10);
        assertTrue(z.collidesWith(right));
        assertFalse(z.collidesWith(top));
        z.update(90 / Zapper.SPIN_SPEED);
        assertFalse(z.collidesWith(right));
        assertTrue(z.collidesWith(top));
    }

    @Test
    void rotatingZapperIgnoresTheEmptyCornersOfItsBox() {
        Zapper z = still(Zapper.Orientation.ROTATING);
        Hitbox corner = new Hitbox(185, 5, 10, 10);
        assertTrue(z.getHitbox().intersects(corner));
        assertFalse(z.collidesWith(corner));
    }
}