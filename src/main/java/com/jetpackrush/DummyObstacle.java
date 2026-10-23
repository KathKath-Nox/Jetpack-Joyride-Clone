package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Obstacle factice : un simple rectangle, qui sert de brique aux tronçons du
 * {@link LevelGenerator} tant que les vrais obstacles ne sont pas branchés dessus.
 */
public class DummyObstacle extends Obstacle {

    static final double DEFAULT_SIZE = 60;

    /**
     * @param x      position horizontale
     * @param y      position verticale
     * @param width  largeur
     * @param height hauteur
     * @param speed  vitesse de défilement du jeu
     */
    public DummyObstacle(double x, double y, double width, double height, GameSpeed speed) {
        super(x, y, width, height, speed);
    }

    @Override
    public ObstacleType getType() {
        return ObstacleType.DUMMY;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.CRIMSON);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeRect(x, y, width, height);
    }
}
