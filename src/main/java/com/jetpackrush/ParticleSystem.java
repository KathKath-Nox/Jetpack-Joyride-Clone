package com.jetpackrush;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Gère les effets visuels d'un joueur : des {@link Particle} (débris, flammes) et des
 * {@link SpriteEffect} (explosion, fumée). Il crée les effets, les met à jour et retire ceux qui
 * sont terminés.
 */
public class ParticleSystem {
    private static final String SMOKE = "/assets/fx_smoke.png";
    private static final String EXPLOSION = "/assets/fx_explosion.png";

    private final List<Particle> particles = new ArrayList<>();
    private final List<SpriteEffect> sprites = new ArrayList<>();
    private final Random random;

    /**
     * Crée un système d'effets avec un générateur aléatoire par défaut.
     */
    public ParticleSystem() {
        this(new Random());
    }

    /**
     * Crée un système d'effets avec un générateur aléatoire donné, ce qui rend les tests
     * reproductibles.
     *
     * @param random générateur aléatoire utilisé pour varier les effets
     */
    public ParticleSystem(Random random) {
        this.random = random;
    }

    private double between(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    /**
     * Ajoute une volute de fumée qui monte vers l'arrière.
     *
     * @param x position horizontale du centre
     * @param y position verticale du centre
     */
    public void smoke(double x, double y) {
        sprites.add(new SpriteEffect(SMOKE, x + between(-20, 20), y + between(-20, 20),
                between(-220, -40), between(-90, -10), between(70, 120), between(6, 9)));
    }

    /**
     * Ajoute un gros nuage de fumée, fait de plusieurs volutes.
     *
     * @param x     position horizontale du centre
     * @param y     position verticale du centre
     * @param count nombre de volutes
     */
    public void cloud(double x, double y, int count) {
        for (int i = 0; i < count; i++) {
            sprites.add(new SpriteEffect(SMOKE, x + between(-30, 30), y + between(-30, 30),
                    between(-150, 80), between(-80, -20), between(100, 170), between(4, 7)));
        }
    }

    /**
     * Ajoute une explosion animée.
     *
     * @param x position horizontale du centre
     * @param y position verticale du centre
     */
    public void explosion(double x, double y) {
        sprites.add(new SpriteEffect(EXPLOSION, x, y, 0, 0, 180, 12));
    }

    /**
     * Projette des débris dorés et gris vers le haut, qui retombent avec la gravité.
     *
     * @param x     position horizontale du point de départ
     * @param y     position verticale du point de départ
     * @param count nombre de débris
     */
    public void burst(double x, double y, int count) {
        for (int i = 0; i < count; i++) {
            Color color = random.nextBoolean() ? Color.rgb(212, 175, 55) : Color.rgb(60, 60, 60);
            particles.add(new Particle(x, y,
                    between(-200, 350), between(-700, -300), 1400,
                    between(10, 22), 0, between(-12, 12), between(1.2, 2.0),
                    color, true));
        }
    }

    /**
     * Ajoute le jet du jetpack pour une émission : deux traînées jaunes qui descendent et, une fois
     * sur trois, une douille orange projetée vers l'arrière.
     *
     * @param x position horizontale de la sortie du jetpack
     * @param y position verticale de la sortie du jetpack
     */
    public void jet(double x, double y) {
        for (int i = 0; i < 2; i++) {
            particles.add(new Particle(x + between(-6, 6), y,
                    between(-60, 20), between(380, 560), 0,
                    between(11, 16), -34, 0, between(0.22, 0.38),
                    Color.rgb(255, 214, 64), false));
        }
        if (random.nextInt(3) == 0) {
            particles.add(new Particle(x, y,
                    between(-380, -120), between(-120, 160), 1100,
                    5, 0, between(-10, 10), between(0.5, 0.8),
                    Color.rgb(224, 120, 40), true));
        }
    }

    /**
     * Met à jour tous les effets et retire ceux qui sont terminés.
     *
     * @param dt temps écoulé depuis la dernière image, en secondes
     */
    public void update(double dt) {
        for (SpriteEffect s : sprites) {
            s.update(dt);
        }
        for (Particle p : particles) {
            p.update(dt);
        }
        sprites.removeIf(s -> !s.isAlive());
        particles.removeIf(p -> !p.isAlive());
    }

    /**
     * Dessine tous les effets : les animations d'abord, puis les particules.
     *
     * @param gc contexte graphique du canvas
     */
    public void render(GraphicsContext gc) {
        for (SpriteEffect s : sprites) {
            s.render(gc);
        }
        for (Particle p : particles) {
            p.render(gc);
        }
    }

    /**
     * Donne le nombre d'effets actifs.
     *
     * @return le nombre de particules et d'animations en cours
     */
    public int size() {
        return particles.size() + sprites.size();
    }
}
