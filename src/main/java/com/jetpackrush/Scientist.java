package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/** Scientifique du laboratoire : il marche, a peur du joueur, est projeté en cas de choc puis s'évanouit (états WALKING, SCARED, LAUNCHED, FAINTED). */
public class Scientist extends Entity {

    /** Largeur du scientifique, en pixels. */
    public static final double WIDTH = 36;
    /** Hauteur du scientifique, en pixels. */
    public static final double HEIGHT = 62;

    private static final double SCALE = 1.1;
    private static final double STEP_TIME = 0.18;
    private static final double FLAME_LENGTH = 120;
    private static final double FLAME_INSET = 8;
    private static final double SCARE_RANGE = 260;
    private static final double KNOCK_SPEED_X = 140;
    private static final double KNOCK_SPEED_Y = -480;
    private static final double GRAVITY = 1600;
    private static final double SPIN_SPEED = 720;

    private static final String STANDING = "scientist_stationary.png";
    private static final String MOVING = "scientist_moving.png";
    private static final String CROUCHING = "scientist_crouching.png";
    private static final String TUMBLING = "scientist_fainting_upsidedown.png";
    private static final String FAINTED = "scientist_fainting.png";

    private enum State {
        WALKING,
        SCARED,
        LAUNCHED,
        FAINTED
    }

    private final GameSpeed speed;
    private final double walkSpeed;
    private final double depth;
    private State state = State.WALKING;
    private double stateTime;
    private double vx;
    private double vy;
    private double spin;

    /**
     * Crée un scientifique.
     *
     * @param x position horizontale de départ
     * @param walkSpeed vitesse de marche (le signe donne la direction)
     * @param depth profondeur d'affichage
     * @param speed vitesse de jeu partagée
     */
    public Scientist(double x, double walkSpeed, double depth, GameSpeed speed) {
        super(x, App.HEIGHT - Decor.FLOOR_HEIGHT + depth - HEIGHT, WIDTH, HEIGHT);
        this.walkSpeed = walkSpeed;
        this.depth = depth;
        this.speed = speed;
    }

    /**
     * @return true si le scientifique marche tranquillement
     */
    public boolean isWalking() {
        return state == State.WALKING;
    }

    /**
     * @return true si le scientifique a peur et s'accroupit
     */
    public boolean isScared() {
        return state == State.SCARED;
    }

    /**
     * @return true si le scientifique a été projeté
     */
    public boolean isKnocked() {
        return state == State.LAUNCHED || state == State.FAINTED;
    }

    /**
     * @return true si le scientifique est évanoui au sol
     */
    public boolean isFainted() {
        return state == State.FAINTED;
    }

    /**
     * @return true si le scientifique est sorti de l'écran
     */
    public boolean isOffScreen() {
        return x + width < 0;
    }

    /**
     * Fait réagir le scientifique à la présence du joueur (peur, ou projection en cas de contact).
     *
     * @param player hitbox du joueur
     * @param thrusting true si le jetpack est en train de propulser
     */
    public void react(Hitbox player, boolean thrusting) {
        if (isKnocked()) {
            return;
        }
        Hitbox body = getHitbox();
        if (player.intersects(body) || (thrusting && flame(player).intersects(body))) {
            knock(player);
        } else if (playerIsNearAbove(player)) {
            enter(State.SCARED);
        } else {
            enter(State.WALKING);
        }
    }

    @Override
    public void update(double dt) {
        stateTime += dt;
        switch (state) {
            case WALKING -> x += (walkSpeed - speed.getSpeed()) * dt;
            case SCARED, FAINTED -> x -= speed.getSpeed() * dt;
            case LAUNCHED -> fly(dt);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        Image image = Sprites.get(spriteName());
        if (image == null) {
            gc.setFill(isKnocked() ? Color.GRAY : Color.BEIGE);
            gc.fillRect(x, y, width, height);
            return;
        }
        double size = image.getWidth() * SCALE;
        double centerX = x + width / 2;
        gc.save();
        if (state == State.LAUNCHED) {
            gc.translate(centerX, y + height / 2);
            gc.rotate(spin);
            gc.drawImage(image, -size / 2, -size / 2, size, size);
        } else {
            gc.translate(centerX, y + height);
            if (walkSpeed > 0) {
                gc.scale(-1, 1);
            }
            gc.drawImage(image, -size / 2, -size, size, size);
        }
        gc.restore();
    }

    private String spriteName() {
        return switch (state) {
            case WALKING -> (int) (stateTime / STEP_TIME) % 2 == 0 ? STANDING : MOVING;
            case SCARED -> CROUCHING;
            case LAUNCHED -> TUMBLING;
            case FAINTED -> FAINTED;
        };
    }

    private Hitbox flame(Hitbox player) {
        return new Hitbox(player.getX() + FLAME_INSET, player.getY() + player.getHeight(),
            player.getWidth() - 2 * FLAME_INSET, FLAME_LENGTH);
    }

    private boolean playerIsNearAbove(Hitbox player) {
        double playerCenter = player.getX() + player.getWidth() / 2;
        boolean above = player.getY() + player.getHeight() <= y;
        return above && Math.abs(playerCenter - (x + width / 2)) < SCARE_RANGE;
    }

    private void knock(Hitbox player) {
        double playerCenter = player.getX() + player.getWidth() / 2;
        double direction = x + width / 2 >= playerCenter ? 1 : -1;
        vx = direction * KNOCK_SPEED_X;
        vy = KNOCK_SPEED_Y;
        spin = 0;
        enter(State.LAUNCHED);
    }

    private void fly(double dt) {
        vy += GRAVITY * dt;
        y += vy * dt;
        x += (vx - speed.getSpeed()) * dt;
        spin = (spin + SPIN_SPEED * dt) % 360;
        double restingY = App.HEIGHT - Decor.FLOOR_HEIGHT + depth - height;
        if (vy > 0 && y >= restingY) {
            y = restingY;
            vx = 0;
            vy = 0;
            spin = 0;
            enter(State.FAINTED);
        }
    }

    private void enter(State next) {
        if (state != next) {
            state = next;
            stateTime = 0;
        }
    }
}