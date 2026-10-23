package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/** Obstacle électrique statique (ou qui tourne) : six orientations, sprite animé sur 4 images. */
public class Zapper extends Obstacle {

    /** Orientation et dimensions d'un zapper. */
    public enum Orientation {
        /** Barre horizontale. */
        HORIZONTAL(160, 28),
        /** Barre verticale. */
        VERTICAL(28, 160),
        /** Barre en diagonale. */
        DIAGONAL(120, 120),
        /** Longue barre horizontale. */
        HORIZONTAL_LONG(520, 28),
        /** Longue barre verticale. */
        VERTICAL_LONG(28, 240),
        /** Barre qui tourne sur elle-même. */
        ROTATING(200, 200);

        private final double width;
        private final double height;

        Orientation(double width, double height) {
            this.width = width;
            this.height = height;
        }

        /**
         * @return la largeur de cette orientation, en pixels
         */
        public double width() {
            return width;
        }

        /**
         * @return la hauteur de cette orientation, en pixels
         */
        public double height() {
            return height;
        }
    }

    /** Vitesse de rotation des zappers qui tournent, en degrés par seconde. */
    public static final double SPIN_SPEED = 110;

    private static final double THICKNESS = 28;
    private static final double CAP_RADIUS = THICKNESS / 2;
    private static final double BAR_WIDTH = 12;
    private static final int STEPS = 10;

    private static final String SHEET = "zapper_sprite_sheet.png";
    private static final int FRAMES = 4;
    private static final double FRAME_RATE = 10;
    private static final double CROP_X = 28;
    private static final double CROP_WIDTH = 54;
    private static final double BASE_LENGTH = 160;
    private static final double TOP_CAP_ROWS = 44;
    private static final double BOTTOM_CAP_START = 80;

    private final Orientation orientation;
    private double animationTime;
    private double spin;

    /**
     * Crée un zapper.
     *
     * @param orientation orientation (et donc taille) du zapper
     * @param x position horizontale
     * @param y position verticale
     * @param speed vitesse de jeu partagée
     */
    public Zapper(Orientation orientation, double x, double y, GameSpeed speed) {
        super(x, y, orientation.width(), orientation.height(), speed);
        this.orientation = orientation;
    }

    /**
     * @return l'orientation du zapper
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * @return l'angle de rotation actuel du zapper
     */
    public double getSpin() {
        return spin;
    }

    @Override
    public ObstacleType getType() {
        return switch (orientation) {
            case HORIZONTAL -> ObstacleType.ZAPPER_HORIZONTAL;
            case VERTICAL -> ObstacleType.ZAPPER_VERTICAL;
            case DIAGONAL -> ObstacleType.ZAPPER_DIAGONAL;
            case HORIZONTAL_LONG -> ObstacleType.ZAPPER_HORIZONTAL_LONG;
            case VERTICAL_LONG -> ObstacleType.ZAPPER_VERTICAL_LONG;
            case ROTATING -> ObstacleType.ZAPPER_ROTATING;
        };
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        animationTime += dt;
        if (orientation == Orientation.ROTATING) {
            spin = (spin + SPIN_SPEED * dt) % 360;
        }
    }

    @Override
    public void reset(double x, double y) {
        super.reset(x, y);
        spin = 0;
    }

    @Override
    public boolean collidesWith(Hitbox other) {
        if (orientation != Orientation.DIAGONAL && orientation != Orientation.ROTATING) {
            return super.collidesWith(other);
        }
        double[] line = line();
        for (int i = 0; i < STEPS; i++) {
            double t = i / (double) (STEPS - 1);
            double cx = line[0] + t * (line[2] - line[0]);
            double cy = line[1] + t * (line[3] - line[1]);
            Hitbox step = new Hitbox(cx - CAP_RADIUS, cy - CAP_RADIUS, THICKNESS, THICKNESS);
            if (step.intersects(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(GraphicsContext gc) {
        Image sheet = Sprites.get(SHEET);
        if (sheet == null) {
            renderShape(gc);
            return;
        }
        double frameWidth = Math.floor(sheet.getWidth() / FRAMES);
        int frame = (int) (animationTime * FRAME_RATE) % FRAMES;
        double sourceX = frame * frameWidth + CROP_X;
        double sourceHeight = sheet.getHeight();
        double scale = BASE_LENGTH / sourceHeight;
        double drawWidth = CROP_WIDTH * scale;
        double length = length();
        double topHeight = TOP_CAP_ROWS * scale;
        double bottomHeight = (sourceHeight - BOTTOM_CAP_START) * scale;
        double middleHeight = Math.max(0, length - topHeight - bottomHeight);
        double left = -drawWidth / 2;
        double top = -length / 2;

        gc.save();
        gc.translate(x + width / 2, y + height / 2);
        gc.rotate(angle());
        if (isSpent()) {
            gc.setGlobalAlpha(0.35);
        }
        gc.drawImage(sheet, sourceX, 0, CROP_WIDTH, TOP_CAP_ROWS,
            left, top, drawWidth, topHeight);
        gc.drawImage(sheet, sourceX, TOP_CAP_ROWS, CROP_WIDTH, BOTTOM_CAP_START - TOP_CAP_ROWS,
            left, top + topHeight, drawWidth, middleHeight);
        gc.drawImage(sheet, sourceX, BOTTOM_CAP_START, CROP_WIDTH, sourceHeight - BOTTOM_CAP_START,
            left, top + topHeight + middleHeight, drawWidth, bottomHeight);
        gc.restore();
    }

    private double length() {
        return orientation == Orientation.DIAGONAL ? BASE_LENGTH : Math.max(width, height);
    }

    private double angle() {
        return switch (orientation) {
            case HORIZONTAL, HORIZONTAL_LONG -> 90;
            case VERTICAL, VERTICAL_LONG -> 0;
            case DIAGONAL -> -45;
            case ROTATING -> spin - 90;
        };
    }

    private void renderShape(GraphicsContext gc) {
        double[] line = line();
        gc.setStroke(isSpent() ? Color.GRAY : Color.GOLD);
        gc.setLineWidth(BAR_WIDTH);
        gc.strokeLine(line[0], line[1], line[2], line[3]);
        gc.setFill(isSpent() ? Color.DARKGRAY : Color.ORANGERED);
        gc.fillOval(line[0] - CAP_RADIUS, line[1] - CAP_RADIUS, THICKNESS, THICKNESS);
        gc.fillOval(line[2] - CAP_RADIUS, line[3] - CAP_RADIUS, THICKNESS, THICKNESS);
    }

    private double[] line() {
        double r = CAP_RADIUS;
        return switch (orientation) {
            case HORIZONTAL, HORIZONTAL_LONG ->
                new double[] {x + r, y + height / 2, x + width - r, y + height / 2};
            case VERTICAL, VERTICAL_LONG ->
                new double[] {x + width / 2, y + r, x + width / 2, y + height - r};
            case DIAGONAL ->
                new double[] {x + r, y + r, x + width - r, y + height - r};
            case ROTATING -> {
                double reach = width / 2 - r;
                double dx = Math.cos(Math.toRadians(spin)) * reach;
                double dy = Math.sin(Math.toRadians(spin)) * reach;
                double cx = x + width / 2;
                double cy = y + height / 2;
                yield new double[] {cx - dx, cy - dy, cx + dx, cy + dy};
            }
        };
    }
}