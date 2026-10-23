package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VehicleTest {
    private static final double DT = 1.0 / 60;

    private final boolean[] thrust = {false};
    private final Player player = new Player(200, 300, () -> thrust[0]);

    private void run(int frames) {
        for (int i = 0; i < frames; i++) {
            player.update(DT);
        }
    }

    private void ride() {
        new VehicleEffect().apply(player);
    }

    private double ground() {
        return App.HEIGHT - Player.GROUND_HEIGHT - player.getHitbox().getHeight();
    }

    @Test
    void pickingUpVehicleStartsRiding() {
        ride();
        assertTrue(player.getState() instanceof Riding);
    }

    @Test
    void jetpackDoesNotWorkWhileRiding() {
        ride();
        thrust[0] = true;
        run(5);
        assertTrue(player.getY() > 300);
    }

    @Test
    void jumpsFromGround() {
        ride();
        run(200);
        assertEquals(ground(), player.getY(), 1e-9);
        thrust[0] = true;
        run(1);
        assertTrue(player.getY() < ground());
    }

    @Test
    void vehicleExpiresAfterTenSeconds() {
        ride();
        run(300);
        assertTrue(player.getState() instanceof Riding);
        run(310);
        assertTrue(player.getState() instanceof Falling);
    }

    @Test
    void vehicleAbsorbsOneHit() {
        ride();
        player.hit();
        assertFalse(player.isDead());
        assertTrue(player.getState() instanceof Falling);
    }

    @Test
    void hitAfterVehicleKills() {
        ride();
        player.hit();
        player.hit();
        assertTrue(player.isDead());
    }

    @Test
    void vehicleHitKeepsShield() {
        player.giveShield();
        ride();
        player.hit();
        assertTrue(player.hasShield());
        assertFalse(player.isDead());
    }

    @Test
    void deadPlayerCannotRide() {
        player.die();
        ride();
        assertTrue(player.isDead());
    }

    @Test
    void expireEndsRiding() {
        ride();
        new VehicleEffect().expire(player);
        assertTrue(player.getState() instanceof Falling);
    }
}
