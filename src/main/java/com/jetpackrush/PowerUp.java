package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Boîte de power-up ramassable (pattern Strategy). Elle ne connaît pas son effet : elle le délègue
 * à un {@link PowerUpEffect}, ce qui permet d'ajouter un power-up sans modifier cette classe.
 */
public class PowerUp extends Item {
    private final PowerUpEffect effect;

    /**
     * Crée un power-up avec une zone de ramassage de 44 par 44 pixels.
     *
     * @param x      position horizontale du coin supérieur gauche
     * @param y      position verticale du coin supérieur gauche
     * @param speed  vitesse de défilement du jeu
     * @param effect effet donné au joueur quand il ramasse la boîte
     */
    public PowerUp(double x, double y, GameSpeed speed, PowerUpEffect effect) {
        super(x, y, 44, 44, speed);
        this.effect = effect;
    }

    /**
     * Applique l'effet au joueur et marque la boîte comme ramassée. Ne fait rien si elle l'est déjà.
     *
     * @param player le joueur qui touche la boîte
     */
    @Override
    public void onCollision(Player player) {
        if (isCollected()) {
            return;
        }
        effect.apply(player);
        collect();
    }

    /**
     * Dessine la boîte avec son image, ou un rond de la couleur de l'effet si l'image manque. Ne
     * dessine rien une fois la boîte ramassée.
     *
     * @param gc contexte graphique du canvas
     */
    @Override
    public void render(GraphicsContext gc) {
        if (isCollected()) {
            return;
        }
        Image image = AssetImages.get("/assets/powerup_box.png");
        if (image == null) {
            gc.setFill(effect.getColor());
            gc.fillOval(x, y, width, height);
            return;
        }
        gc.save();
        gc.setImageSmoothing(false);
        gc.drawImage(image, x - 10, y - 10, 64, 64);
        gc.restore();
    }
}
