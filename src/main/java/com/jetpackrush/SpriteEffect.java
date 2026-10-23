package com.jetpackrush;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Effet visuel animé joué une seule fois, à partir d'une image en bande verticale de 6 cases de
 * 64 par 64 pixels (explosion, fumée). Il se déplace à une vitesse donnée et disparaît à la fin de
 * l'animation.
 */
public class SpriteEffect {
    /** Nombre d'images de l'animation. */
    public static final int FRAMES = 6;
    private static final int FRAME_SIZE = 64;
    private static final Map<String, Image> CACHE = new HashMap<>();

    private final String path;
    private final double vx;
    private final double vy;
    private final double size;
    private final double fps;
    private double x;
    private double y;
    private double age;

    /**
     * Crée un effet animé.
     *
     * @param path chemin de l'image dans les ressources
     * @param x    position horizontale du centre
     * @param y    position verticale du centre
     * @param vx   vitesse horizontale, en pixels par seconde
     * @param vy   vitesse verticale, en pixels par seconde
     * @param size taille d'affichage, en pixels
     * @param fps  nombre d'images de l'animation par seconde
     */
    public SpriteEffect(String path, double x, double y, double vx, double vy, double size, double fps) {
        this.path = path;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
        this.fps = fps;
    }

    /**
     * Fait avancer l'animation et déplace l'effet.
     *
     * @param dt temps écoulé depuis la dernière image, en secondes
     */
    public void update(double dt) {
        age += dt;
        x += vx * dt;
        y += vy * dt;
    }

    /**
     * Donne l'image courante de l'animation.
     *
     * @return l'indice de l'image, entre 0 et {@link #FRAMES} moins 1
     */
    public int getFrame() {
        return Math.min(FRAMES - 1, (int) (age * fps));
    }

    /**
     * Indique si l'animation n'est pas terminée.
     *
     * @return {@code true} tant que l'effet doit être affiché
     */
    public boolean isAlive() {
        return age * fps < FRAMES;
    }

    /**
     * Dessine l'image courante de l'effet. Ne dessine rien si l'image est introuvable.
     *
     * @param gc contexte graphique du canvas
     */
    public void render(GraphicsContext gc) {
        Image image = load(path);
        if (image == null) {
            return;
        }
        gc.save();
        gc.setImageSmoothing(false);
        gc.drawImage(image, 0, getFrame() * FRAME_SIZE, FRAME_SIZE, FRAME_SIZE,
                x - size / 2, y - size / 2, size, size);
        gc.restore();
    }

    private static Image load(String path) {
        if (CACHE.containsKey(path)) {
            return CACHE.get(path);
        }
        InputStream in = SpriteEffect.class.getResourceAsStream(path);
        Image image = in == null ? null : new Image(in);
        CACHE.put(path, image);
        return image;
    }
}
