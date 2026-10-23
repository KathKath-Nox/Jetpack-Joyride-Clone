package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Pièce à ramasser : défile avec le décor et tourne sur elle-même. Ramassée, elle prévient
 * le {@link ScoreManager}.
 */
public class Coin extends Item {

    static final double SIZE = 32;
    private static final int FRAMES = 6;
    private static final double FRAME_TIME = 0.1;

    private final ScoreManager score;
    private double animTime;

    /**
     * @param x     position horizontale de départ
     * @param y     position verticale
     * @param speed vitesse de défilement du jeu
     * @param score le score à créditer quand la pièce est ramassée
     */
    public Coin(double x, double y, GameSpeed speed, ScoreManager score) {
        super(x, y, SIZE, SIZE, speed);
        this.score = score;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        animTime += dt;
    }

    @Override
    public void onCollision(Player player) {
        if (isCollected()) {
            return;
        }
        collect();
        score.addCoin();
    }

    @Override
    public void render(GraphicsContext gc) {
        if (isCollected()) {
            return;
        }
        Image sheet = AssetImages.get("/assets/coins_sprite_sheet.png");
        if (sheet == null) {
            gc.setFill(Color.GOLD);
            gc.fillOval(x, y, width, height);
            return;
        }
        double frameWidth = sheet.getWidth() / FRAMES;
        int frame = (int) (animTime / FRAME_TIME) % FRAMES;
        gc.save();
        gc.setImageSmoothing(false);
        gc.drawImage(sheet, frame * frameWidth, 0, frameWidth, sheet.getHeight(), x, y, width, height);
        gc.restore();
    }
}
