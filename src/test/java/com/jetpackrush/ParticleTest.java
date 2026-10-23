package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

class ParticleTest {

    private Particle particle(double gravity, double lifetime) {
        return new Particle(0, 0, 0, 0, gravity, 10, 0, 0, lifetime, Color.WHITE, false);
    }

    @Test
    void particleDiesAfterItsLifetime() {
        Particle p = particle(0, 1.0);
        p.update(0.5);
        assertTrue(p.isAlive());
        p.update(0.6);
        assertFalse(p.isAlive());
    }

    @Test
    void alphaFadesOut() {
        Particle p = particle(0, 1.0);
        p.update(0.5);
        assertEquals(0.5, p.getAlpha(), 1e-9);
    }

    @Test
    void gravityPullsParticleDown() {
        Particle p = particle(1000, 5.0);
        p.update(0.1);
        assertTrue(p.getY() > 0);
    }

    @Test
    void burstCreatesRequestedCount() {
        ParticleSystem system = new ParticleSystem(new Random(1));
        system.burst(0, 0, 12);
        assertEquals(12, system.size());
    }

    @Test
    void smokeAddsOnePuff() {
        ParticleSystem system = new ParticleSystem(new Random(1));
        system.smoke(0, 0);
        assertEquals(1, system.size());
    }

    @Test
    void deadParticlesAreRemoved() {
        ParticleSystem system = new ParticleSystem(new Random(1));
        system.burst(0, 0, 5);
        system.update(5.0);
        assertEquals(0, system.size());
    }
}
