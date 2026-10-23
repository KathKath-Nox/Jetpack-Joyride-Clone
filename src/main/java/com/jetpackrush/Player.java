package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Le joueur. Il gère sa physique verticale (gravité, poussée du jetpack, saut du véhicule), son état
 * courant (pattern State), son bouclier et ses effets visuels. Il lit ses commandes uniquement via
 * une {@link InputSource}.
 */
public class Player extends Entity {
    /** Hauteur du sol en pixels, mesurée depuis le bas de l'écran. */
    public static final double GROUND_HEIGHT = 80;   // hauteur du sol en pixels, mesurée depuis le bas de l'écran

    private static final double GRAVITY = 1400;      // accélération vers le bas, en px/s², qui fait retomber le joueur
    private static final double THRUST = 2800;       // accélération vers le haut du jetpack quand Espace est enfoncé, en px/s²
    private static final double MAX_SPEED = 500;     // vitesse verticale maximale (haut et bas), en px/s
    private static final double JUMP_SPEED = 850;    // vitesse initiale d'un saut du véhicule, en px/s

    private final InputSource input;
    private final PlayerSprite sprite = new PlayerSprite();
    private String skin = PlayerSprite.PATH;
    private PlayerState state = new Falling();
    private double vy;
    private double animationTime;
    private boolean shielded;
    private final ParticleSystem effects = new ParticleSystem();
    private double smokeTimer;
    private double flameTimer;
    private double smokeLeft;
    private GameEvents events = new GameEvents();
    private boolean jetpackOn;

    /**
     * Crée un joueur de 40 par 60 pixels, qui commence en {@link Falling}.
     *
     * @param x     position horizontale initiale
     * @param y     position verticale initiale
     * @param input source d'entrée qui fournit la commande de poussée
     */
    public Player(double x, double y, InputSource input) {
        super(x, y, 40, 60);
        this.input = input;
    }

    /**
     * Choisit où le joueur publie ses événements (protection perdue, choc mortel).
     *
     * @param events le sujet auquel s'abonnent les observateurs de la partie
     */
    public void setEvents(GameEvents events) {
        this.events = events;
    }

    /**
     * Met à jour le joueur pour une image : fait avancer l'animation, délègue à l'état courant puis
     * met à jour les effets visuels.
     *
     * @param dt temps écoulé depuis la dernière image, en secondes
     */
    @Override
    public void update(double dt) {
        animationTime += dt;
        state.update(this, dt);
        updateEffects(dt);
        updateJetpackSound();
    }

    // Le jetpack ne pousse ni mort ni dans un véhicule (la touche sert alors à sauter)
    private void updateJetpackSound() {
        boolean on = !isDead() && !(state instanceof Riding) && input.isThrustPressed();
        if (on != jetpackOn) {
            jetpackOn = on;
            events.publish(on ? GameEvent.THRUST_STARTED : GameEvent.THRUST_STOPPED);
        }
    }

    /**
     * Relance le son du jetpack après une pause, si la poussée est toujours maintenue.
     */
    public void resumeSounds() {
        if (jetpackOn) {
            events.publish(GameEvent.THRUST_STARTED);
        }
    }

    /**
     * Applique la physique du jetpack : la gravité, moins la poussée si elle est demandée.
     *
     * @param dt        temps écoulé depuis la dernière image, en secondes
     * @param thrusting {@code true} si le jetpack pousse
     */
    void applyPhysics(double dt, boolean thrusting) {
        step(dt, GRAVITY - (thrusting ? THRUST : 0), MAX_SPEED);
    }

    /**
     * Applique la physique du véhicule qui court ou saute : la gravité seule, avec la vitesse de saut
     * comme vitesse maximale.
     *
     * @param dt temps écoulé depuis la dernière image, en secondes
     */
    void applyRidingPhysics(double dt) {
        step(dt, GRAVITY, JUMP_SPEED);
    }

    private void step(double dt, double acceleration, double maxSpeed) {
        vy = Math.max(-maxSpeed, Math.min(maxSpeed, vy + acceleration * dt));
        y += vy * dt;

        if (y < 0) {
            y = 0;
            vy = 0;
        }
        double maxY = groundY();
        if (y > maxY) {
            y = maxY;
            vy = 0;
        }
    }

    private double groundY() {
        return App.HEIGHT - GROUND_HEIGHT - height;
    }

    /**
     * Indique si le joueur touche le sol.
     *
     * @return {@code true} s'il est au sol
     */
    boolean isOnGround() {
        return y >= groundY();
    }

    /**
     * Lance un saut : donne au joueur une vitesse initiale vers le haut.
     */
    void jump() {
        vy = -JUMP_SPEED;
    }

    /**
     * Indique si la poussée est demandée par la source d'entrée.
     *
     * @return {@code true} tant que la touche de poussée est maintenue
     */
    boolean isThrustPressed() {
        return input.isThrustPressed();
    }

    /**
     * Change l'état courant du joueur.
     *
     * @param state le nouvel état
     */
    void setState(PlayerState state) {
        this.state = state;
    }

    /**
     * Donne l'état courant du joueur.
     *
     * @return l'état courant
     */
    public PlayerState getState() {
        return state;
    }

    /**
     * Applique un choc mortel. Dans l'ordre : le véhicule est détruit et le joueur survit, sinon le
     * bouclier est consommé, sinon le joueur meurt. Les obstacles appellent cette méthode, jamais
     * {@link #die()} directement.
     */
    public void hit() {
        if (state instanceof Riding) {
            state = new Falling();
            effects.burst(centerX(), centerY(), 22);
            effects.explosion(centerX(), centerY());
            effects.cloud(centerX(), centerY(), 16);
            smokeLeft = 1.2;
            events.publish(GameEvent.PROTECTION_LOST);
            return;
        }
        if (shielded) {
            shielded = false;
            events.publish(GameEvent.PROTECTION_LOST);
            return;
        }
        die();
    }

    /**
     * Fait mourir le joueur et lance l'effet de fumée. Ne fait rien s'il est déjà mort.
     */
    public void die() {
        if (isDead()) {
            return;
        }
        state = new Dead();
        effects.cloud(centerX(), centerY(), 10);
        smokeLeft = 2.0;
        events.publish(GameEvent.PLAYER_SHOCKED);
    }

    /**
     * Lance l'effet d'entrée en jeu : explosion, débris et fumée, comme si le personnage cassait un
     * mur. Appelée au lancement et au redémarrage.
     */
    public void enter() {
        effects.explosion(centerX(), centerY());
        effects.burst(centerX(), centerY(), 18);
        effects.cloud(centerX(), centerY(), 12);
        smokeLeft = 0.8;
    }

    private void updateEffects(double dt) {
        if (smokeLeft > 0) {
            smokeLeft -= dt;
            smokeTimer += dt;
            if (smokeTimer >= 0.03) {
                smokeTimer = 0;
                effects.smoke(centerX(), centerY());
            }
        }
        if (getState() instanceof Flying) {
            flameTimer += dt;
            while (flameTimer >= 0.02) {
                flameTimer -= 0.02;
                effects.jet(centerX() - 12, centerY() + 24);
            }
        } else {
            flameTimer = 0;
        }
        effects.update(dt);
    }

    private double centerX() {
        return x + width / 2;
    }

    private double centerY() {
        return y + height / 2;
    }

    /**
     * Donne le nombre d'effets visuels actifs.
     *
     * @return le nombre d'effets en cours
     */
    int effectCount() {
        return effects.size();
    }

    /**
     * Indique si le joueur est mort.
     *
     * @return {@code true} s'il est dans l'état {@link Dead}
     */
    public boolean isDead() {
        return state instanceof Dead;
    }

    /**
     * Donne un bouclier au joueur.
     */
    public void giveShield() {
        shielded = true;
    }

    /**
     * Retire le bouclier du joueur.
     */
    public void removeShield() {
        shielded = false;
    }

    /**
     * Indique si le joueur a un bouclier.
     *
     * @return {@code true} s'il est protégé
     */
    public boolean hasShield() {
        return shielded;
    }

    /**
     * Donne la vitesse verticale du joueur.
     *
     * @return la vitesse verticale en pixels par seconde, négative vers le haut
     */
    public double getVelocityY() {
        return vy;
    }

    private void renderVehicle(GraphicsContext gc) {
        String name = "vehicle_ground2";
        if (state instanceof Riding && ((Riding) state).isThrusting()) {
            name = "vehicle_rising";
        } else if (isOnGround()) {
            name = (int) (animationTime * 8) % 2 == 0 ? "vehicle_ground" : "vehicle_ground2";
        }
        Image image = AssetImages.get("/assets/" + name + ".png");
        if (image == null) {
            gc.setFill(Color.ORANGE);
            gc.fillRect(x - 15, y + height - 15, width + 30, 25);
            return;
        }
        double w = image.getWidth() * 1.25;
        double h = image.getHeight() * 1.25;
        gc.save();
        gc.setImageSmoothing(false);
        gc.drawImage(image, x + width / 2 - w / 2, y + height - h + 8, w, h);
        gc.restore();
    }

    private void renderShield(GraphicsContext gc) {
        Image image = AssetImages.get("/assets/shield_bubble.png");
        if (image == null) {
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(3);
            gc.strokeOval(x - 10, y - 10, width + 20, height + 20);
            return;
        }
        double size = 110;
        gc.save();
        gc.setImageSmoothing(false);
        gc.drawImage(image, x + width / 2 - size / 2, y + height / 2 - size / 2, size, size);
        gc.restore();
    }

    /**
     * Dessine le joueur : son sprite, ou le véhicule s'il roule, puis les effets et la bulle du
     * bouclier s'il est protégé.
     *
     * @param gc contexte graphique du canvas
     */
    @Override
    public void render(GraphicsContext gc) {
        if (state instanceof Riding) {
            renderVehicle(gc);
        } else {
            sprite.render(gc, skin, state, animationTime, getHitbox());
        }
        effects.render(gc);
        if (shielded) {
            renderShield(gc);
        }
    }

    /**
     * Change le skin du joueur.
     *
     * @param path chemin de la feuille de sprites à utiliser
     */
    public void setSkin(String path) {
        this.skin = path;
    }

    /**
     * Donne le skin du joueur.
     *
     * @return le chemin de la feuille de sprites utilisée
     */
    public String getSkin() {
        return skin;
    }
}
