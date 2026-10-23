package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/** Laser horizontal : un avertissement, puis un faisceau actif, puis il disparaît (phases WARNING, ACTIVE, DONE). */
public class Laser extends Obstacle {

    /** Épaisseur du faisceau, en pixels. */
    public static final double THICKNESS = 48;
    /** Durée de l'avertissement avant que le faisceau devienne actif, en secondes. */
    public static final double WARNING_TIME = 1.2;
    /** Durée pendant laquelle le faisceau est actif, en secondes. */
    public static final double ACTIVE_TIME = 1.0;

    private static final double URGENT_TIME = 0.4;
    private static final String BEAM = "laserbeam.png";
    private static final String EMITTER = "laseremitter.png";
    private static final String MUZZLE = "laserfiring.png";
    private static final double BEAM_SCALE = 1.4;
    private static final double BEAM_CENTER_ROW = 18.5;
    private static final double BEAM_SCROLL = 900;
    private static final double MUZZLE_SCALE = 1.6;
    private static final double MUZZLE_CENTER_ROW = 49.5;
    private static final double EMITTER_SCALE = 1.5;

    private enum Phase {
        WARNING,
        ACTIVE,
        DONE
    }

    private Phase phase = Phase.WARNING;
    private double timer;

    /**
     * Crée un laser.
     *
     * @param y position verticale du faisceau
     * @param speed vitesse de jeu partagée
     */
    public Laser(double y, GameSpeed speed) {
        super(0, y, App.WIDTH, THICKNESS, speed);
    }

    @Override
    public ObstacleType getType() {
        return ObstacleType.LASER;
    }

    /**
     * @return true si le laser est en phase d'avertissement
     */
    public boolean isWarning() {
        return phase == Phase.WARNING;
    }

    /**
     * @return true si le faisceau est actif (dangereux)
     */
    public boolean isActive() {
        return phase == Phase.ACTIVE;
    }

    /**
     * @return true si le laser a fini son cycle
     */
    public boolean isDone() {
        return phase == Phase.DONE;
    }

    @Override
    public void update(double dt) {
        timer += dt;
        if (phase == Phase.WARNING && timer >= WARNING_TIME) {
            timer -= WARNING_TIME;
            phase = Phase.ACTIVE;
        }
        if (phase == Phase.ACTIVE && timer >= ACTIVE_TIME) {
            phase = Phase.DONE;
        }
    }

    @Override
    public boolean collidesWith(Hitbox other) {
        return phase == Phase.ACTIVE && super.collidesWith(other);
    }

    @Override
    public void onCollision(Player player) {
        if (phase == Phase.ACTIVE) {
            super.onCollision(player);
        }
    }

    @Override
    public boolean isOffScreen() {
        return phase == Phase.DONE;
    }

    @Override
    public void reset(double x, double y) {
        super.reset(0, y);
        phase = Phase.WARNING;
        timer = 0;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (phase == Phase.WARNING) {
            renderWarning(gc);
        } else if (phase == Phase.ACTIVE) {
            renderBeam(gc);
        }
    }

    private void renderWarning(GraphicsContext gc) {
        double centerY = y + height / 2;
        boolean urgent = timer >= WARNING_TIME - URGENT_TIME;
        int blink = (int) (timer * (urgent ? 14 : 6));
        gc.setGlobalAlpha(blink % 2 == 0 ? 0.35 : 0.95);
        gc.setStroke(Color.RED);
        gc.setLineWidth(urgent ? 6 : 3);
        gc.strokeLine(0, centerY, App.WIDTH, centerY);
        gc.setGlobalAlpha(1);
        drawEmitter(gc, centerY, false);
        drawEmitter(gc, centerY, true);
    }

    private void renderBeam(GraphicsContext gc) {
        Image muzzle = Sprites.get(MUZZLE);
        double centerY = y + height / 2;
        double beamStart = muzzle == null ? 0 : muzzle.getWidth() * MUZZLE_SCALE;
        if (isSpent()) {
            gc.setGlobalAlpha(0.45);
        }
        drawBeamBody(gc, Sprites.get(BEAM), beamStart, centerY);
        if (muzzle != null) {
            gc.drawImage(muzzle, 0, centerY - MUZZLE_CENTER_ROW * MUZZLE_SCALE,
                muzzle.getWidth() * MUZZLE_SCALE, muzzle.getHeight() * MUZZLE_SCALE);
        }
        drawEmitter(gc, centerY, true);
        gc.setGlobalAlpha(1);
    }

    private void drawBeamBody(GraphicsContext gc, Image beam, double start, double centerY) {
        if (beam == null) {
            gc.setFill(beamGradient());
            gc.fillRect(start - 1, y, App.WIDTH - start + 1, height);
            return;
        }
        double tile = beam.getWidth() * BEAM_SCALE;
        double drawHeight = beam.getHeight() * BEAM_SCALE;
        double top = centerY - BEAM_CENTER_ROW * BEAM_SCALE;
        double shift = (timer * BEAM_SCROLL) % tile;
        gc.save();
        gc.beginPath();
        gc.rect(start - 1, top, App.WIDTH - start + 1, drawHeight);
        gc.clip();
        for (double x = start - shift; x < App.WIDTH; x += tile) {
            gc.drawImage(beam, x, top, tile + 1, drawHeight);
        }
        gc.restore();
    }

    private void drawEmitter(GraphicsContext gc, double centerY, boolean right) {
        Image emitter = Sprites.get(EMITTER);
        if (emitter == null) {
            return;
        }
        double w = emitter.getWidth() * EMITTER_SCALE;
        double h = emitter.getHeight() * EMITTER_SCALE;
        gc.save();
        if (right) {
            gc.translate(App.WIDTH, centerY - h / 2);
            gc.scale(-1, 1);
        } else {
            gc.translate(0, centerY - h / 2);
        }
        gc.drawImage(emitter, 0, 0, w, h);
        gc.restore();
    }

    private LinearGradient beamGradient() {
        return new LinearGradient(0, y, 0, y + height, false, CycleMethod.NO_CYCLE,
            new Stop(0.00, Color.rgb(255, 0, 0)),
            new Stop(0.17, Color.rgb(255, 230, 120)),
            new Stop(0.33, Color.rgb(254, 255, 224)),
            new Stop(0.50, Color.rgb(255, 255, 252)),
            new Stop(0.67, Color.rgb(253, 253, 136)),
            new Stop(0.83, Color.rgb(255, 47, 17)),
            new Stop(1.00, Color.rgb(255, 0, 0, 0)));
    }
}