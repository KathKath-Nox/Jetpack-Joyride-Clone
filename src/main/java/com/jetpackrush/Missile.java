package com.jetpackrush;

import java.util.function.Supplier;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/** Missile : une alerte apparaît d'abord et suit le joueur, puis le missile fonce vers la gauche (phases WARNING puis FLYING). */
public class Missile extends Obstacle {

    /** Largeur du missile, en pixels. */
    public static final double WIDTH = 70;
    /** Hauteur du missile, en pixels. */
    public static final double HEIGHT = 26;
    /** Durée de l'avertissement avant le tir, en secondes. */
    public static final double WARNING_TIME = 1.5;
    /** Durée avant le tir pendant laquelle la position visée n'est plus mise à jour, en secondes. */
    public static final double LOCK_TIME = 0.5;
    /** Vitesse supplémentaire du missile par rapport au défilement, en pixels par seconde. */
    public static final double EXTRA_SPEED = 500;

    private static final double ALERT_X = App.WIDTH - 60;
    private static final double ALERT_SIZE = 44;
    private static final double ICON_SIZE = 56;

    private static final String BODY = "missile.png";
    private static final String COMING = "missile_coming.png";
    private static final String TARGET = "missile_target.png";

    private enum Phase {
        WARNING,
        FLYING
    }

    private final Supplier<Hitbox> target;
    private Phase phase = Phase.WARNING;
    private double timer;

    /**
     * Crée un missile.
     *
     * @param x position horizontale de départ
     * @param y position verticale de départ
     * @param speed vitesse de jeu partagée
     * @param target fournit la hitbox du joueur visé
     */
    public Missile(double x, double y, GameSpeed speed, Supplier<Hitbox> target) {
        super(x, y, WIDTH, HEIGHT, speed);
        this.target = target;
    }

    @Override
    public ObstacleType getType() {
        return ObstacleType.MISSILE;
    }

    /**
     * @return true si le missile est encore en phase d'avertissement
     */
    public boolean isWarning() {
        return phase == Phase.WARNING;
    }

    /**
     * @return true si le missile est en train de voler
     */
    public boolean isFlying() {
        return phase == Phase.FLYING;
    }

    /**
     * @return la position verticale de l'alerte affichée avant le tir
     */
    public double getAlertY() {
        return y + HEIGHT / 2;
    }

    @Override
    public void update(double dt) {
        if (phase == Phase.WARNING) {
            if (timer < WARNING_TIME - LOCK_TIME) {
                aim();
            }
            timer += dt;
            if (timer >= WARNING_TIME) {
                phase = Phase.FLYING;
            }
            return;
        }
        x -= (getSpeed().getSpeed() + EXTRA_SPEED) * dt;
    }

    private void aim() {
        Hitbox aimed = target.get();
        if (aimed == null) {
            return;
        }
        double centered = aimed.getY() + aimed.getHeight() / 2 - HEIGHT / 2;
        double lowest = App.HEIGHT - Decor.FLOOR_HEIGHT - HEIGHT;
        y = Math.max(0, Math.min(lowest, centered));
    }

    @Override
    public boolean collidesWith(Hitbox other) {
        return phase == Phase.FLYING && super.collidesWith(other);
    }

    @Override
    public void onCollision(Player player) {
        if (phase == Phase.FLYING) {
            super.onCollision(player);
        }
    }

    @Override
    public void reset(double x, double y) {
        super.reset(x, y);
        phase = Phase.WARNING;
        timer = 0;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (phase == Phase.WARNING) {
            renderAlert(gc);
            return;
        }
        Image body = Sprites.get(BODY);
        if (body == null) {
            renderShape(gc);
            return;
        }
        if (isSpent()) {
            gc.setGlobalAlpha(0.35);
        }
        gc.drawImage(body, x, y - (body.getHeight() - HEIGHT) / 2, body.getWidth(), body.getHeight());
        gc.setGlobalAlpha(1);
    }

    private void renderAlert(GraphicsContext gc) {
        boolean locked = timer >= WARNING_TIME - LOCK_TIME;
        if (locked && ((int) (timer * 10)) % 2 == 0) {
            return;
        }
        double centerY = getAlertY();
        Image icon = Sprites.get(locked ? TARGET : COMING);
        if (icon == null) {
            renderAlertShape(gc, centerY);
            return;
        }
        double centerX = ALERT_X + ALERT_SIZE / 2;
        gc.drawImage(icon, centerX - ICON_SIZE / 2, centerY - ICON_SIZE / 2, ICON_SIZE, ICON_SIZE);
    }

    private void renderShape(GraphicsContext gc) {
        gc.setFill(isSpent() ? Color.DARKGRAY : Color.DARKRED);
        gc.fillRoundRect(x, y, WIDTH, HEIGHT, 12, 12);
        gc.setFill(isSpent() ? Color.GRAY : Color.LIGHTGRAY);
        gc.fillRoundRect(x, y, 16, HEIGHT, 12, 12);
        gc.setFill(isSpent() ? Color.GRAY : Color.ORANGE);
        gc.fillPolygon(
            new double[] {x + WIDTH, x + WIDTH + 26, x + WIDTH},
            new double[] {y + 4, y + HEIGHT / 2, y + HEIGHT - 4},
            3);
    }

    private void renderAlertShape(GraphicsContext gc, double centerY) {
        gc.setFill(Color.RED);
        gc.fillPolygon(
            new double[] {ALERT_X, ALERT_X + ALERT_SIZE, ALERT_X + ALERT_SIZE / 2},
            new double[] {centerY + ALERT_SIZE / 2, centerY + ALERT_SIZE / 2, centerY - ALERT_SIZE / 2},
            3);
        gc.setFill(Color.WHITE);
        gc.fillText("!", ALERT_X + ALERT_SIZE / 2 - 3, centerY + 14);
    }
}
