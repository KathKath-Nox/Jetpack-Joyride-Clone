package com.jetpackrush;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.canvas.GraphicsContext;

/**
 * Écran de jeu : une partie en cours (intro, décor, joueur, obstacles, scientifiques et power-ups).
 * Échap met la partie en pause.
 *
 * <p>En mode Rush ({@link RushMode}), le temps de jeu passe plus vite, les décors changent souvent
 * et pièces et power-ups apparaissent plus souvent : la partie est faite pour la démo.
 *
 * <p>Quand le joueur meurt, le décor se fige et le corps retombe pendant
 * {@link #DEATH_DELAY} secondes, puis la partie passe à l'écran de fin.
 */
public class PlayState implements GameState {

    /** Temps laissé au joueur mort pour retomber avant l'écran de fin, en secondes. */
    static final double DEATH_DELAY = 1.5;

    // Spawner provisoire : sera remplacé par le générateur de niveau (feat/level-generator).
    static final double WALK_SPEED = 220;
    static final double BURST_SPEED = 1000;
    static final double BURST_TIME = 1.4;
    private static final double PLAYER_FRONT = 240;
    private static final double START_SPEED = 220;
    private static final double MAX_SPEED = 520;
    private static final double SPEED_GAIN = 4;
    private static final double FIRST_SPAWN = 3.5;
    private static final double START_DELAY = 3.2;
    private static final double MIN_DELAY = 1.1;
    private static final double DELAY_DROP = 0.04;
    private static final double DIAGONAL_FROM = 10;
    private static final double MISSILE_FROM = 20;
    private static final double ROTATING_FROM = 25;
    private static final double PATTERNS_FROM = 20;
    private static final double PATTERN_COOLDOWN = 30;
    private static final double PATTERN_CHANCE = 0.35;
    private static final int MAX_PATTERNS = 3;
    private static final double LASER_FROM = 40;
    private static final double LASER_EARLY_COOLDOWN = 60;
    private static final double LASER_LATE_COOLDOWN = 90;
    private static final double LASER_LATE_AFTER = 180;
    private static final double LASER_LOCKOUT = 15;
    private static final double DOUBLE_FROM = 70;
    private static final double DOUBLE_COOLDOWN = 50;
    private static final double AFTER_DOUBLE_DELAY = 2.5;
    private static final double DOUBLE_GAP = 220;
    private static final double CORRIDOR_GAP = 300;
    private static final double GATE_GAP = 260;
    private static final double SCIENTIST_FIRST = 1.5;
    private static final double SCIENTIST_MIN_DELAY = 3;
    private static final double SCIENTIST_MAX_DELAY = 6;
    private static final double SPAWN_X = App.WIDTH + 50;
    private static final double START_Y = App.HEIGHT - Player.GROUND_HEIGHT - 60;
    private static final double POWERUP_FIRST = 6;
    private static final double POWERUP_MIN_DELAY = 14;
    private static final double POWERUP_MAX_DELAY = 24;
    private static final double COIN_FIRST = 4;
    private static final double COIN_MIN_DELAY = 3;
    private static final double COIN_MAX_DELAY = 6;
    private static final double COIN_SPACING = 46;
    private static final double COIN_CLEARANCE = 120;

    private final Game game;
    private final InputSource input;
    private final GameSpeed speed = new GameSpeed(BURST_SPEED);
    private final Decor decor;
    private final boolean rush;
    private final double delayScale;
    private final Player player;
    private final GameEvents events = new GameEvents();
    private final PowerUpIndicator indicator = new PowerUpIndicator();
    private final ObstaclePool pool;
    private final ScoreManager score = new ScoreManager();
    private final ScoreHud scoreHud = new ScoreHud();
    private final IntroWall wall = new IntroWall(Decor.ENTRY_X, speed);
    private boolean intro = true;
    private double introTime;
    private final Random random = new Random();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Obstacle> doubleGroup = new ArrayList<>();
    private final List<Scientist> scientists = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();
    private double spawnTimer = FIRST_SPAWN;
    private double scientistTimer = SCIENTIST_FIRST;
    private double powerUpTimer = POWERUP_FIRST;
    private double coinTimer = COIN_FIRST;
    private double elapsed;
    private int patterns;
    private double nextPatternAt = PATTERNS_FROM;
    private double nextLaserAt = LASER_FROM;
    private double lastLaserAt = -LASER_LOCKOUT;
    private int doublesPerGame;
    private int doublesDone;
    private double nextDoubleAt = DOUBLE_FROM;
    private boolean doubleDue;
    private double deathTime;

    /**
     * Prépare une nouvelle partie.
     *
     * @param game  le jeu, pour passer en pause ou à l'écran de fin
     * @param input l'entrée lue par le joueur
     */
    public PlayState(Game game, InputSource input) {
        this(game, input, false);
    }

    /**
     * Prépare une nouvelle partie, normale ou en mode Rush.
     *
     * @param game  le jeu, pour passer en pause ou à l'écran de fin
     * @param input l'entrée lue par le joueur
     * @param rush  {@code true} pour le mode Rush (partie accélérée pour la démo)
     */
    public PlayState(Game game, InputSource input, boolean rush) {
        this.game = game;
        this.input = input;
        this.rush = rush;
        this.delayScale = rush ? RushMode.SPAWN_SCALE : 1;
        this.decor = new Decor(speed, rush ? RushMode.ZONE_LENGTH : Decor.ZONE_LENGTH);
        this.powerUpTimer = POWERUP_FIRST * delayScale;
        this.coinTimer = COIN_FIRST * delayScale;
        this.player = new Player(200, START_Y, input);
        this.player.setEvents(events);
        this.pool = new ObstaclePool(new ObstacleFactory(speed, () -> player.getHitbox()));
        score.addListener(scoreHud);
        scoreHud.setRush(rush);
        planDoubles();
    }

    private void planDoubles() {
        doublesPerGame = rush ? RushMode.MAX_DOUBLES : 2 + random.nextInt(2);
    }

    private double spawnDelay() {
        return Math.max(rush ? RushMode.MIN_DELAY : MIN_DELAY, START_DELAY - elapsed * DELAY_DROP);
    }

    private ObstacleType pickType() {
        List<ObstacleType> types = new ArrayList<>();
        types.add(ObstacleType.ZAPPER_HORIZONTAL);
        types.add(ObstacleType.ZAPPER_VERTICAL);
        if (elapsed >= DIAGONAL_FROM) {
            types.add(ObstacleType.ZAPPER_DIAGONAL);
        }
        if (elapsed >= MISSILE_FROM) {
            types.add(ObstacleType.MISSILE);
        }
        if (elapsed >= ROTATING_FROM) {
            types.add(ObstacleType.ZAPPER_ROTATING);
        }
        return types.get(random.nextInt(types.size()));
    }

    private boolean longObstacleOnScreen() {
        return obstacles.stream().anyMatch(o ->
            o.getType() == ObstacleType.ZAPPER_HORIZONTAL_LONG
                || o.getType() == ObstacleType.ZAPPER_VERTICAL_LONG);
    }

    private boolean laserAllowed() {
        return elapsed - lastLaserAt >= LASER_LOCKOUT;
    }

    private double spawn() {
        if (elapsed >= nextLaserAt && laserAllowed() && !longObstacleOnScreen()) {
            double cooldown = elapsed >= LASER_LATE_AFTER ? LASER_LATE_COOLDOWN : LASER_EARLY_COOLDOWN;
            nextLaserAt = elapsed + cooldown;
            lastLaserAt = elapsed;
            spawnSingle(ObstacleType.LASER);
            return 0;
        }
        int maxPatterns = rush ? RushMode.MAX_PATTERNS : MAX_PATTERNS;
        if (patterns < maxPatterns && elapsed >= nextPatternAt && random.nextDouble() < PATTERN_CHANCE) {
            patterns++;
            nextPatternAt = elapsed + PATTERN_COOLDOWN;
            if (random.nextBoolean()) {
                spawnCorridor();
                return Zapper.Orientation.HORIZONTAL_LONG.width();
            }
            spawnGate();
            return 0;
        }
        spawnSingle(pickType());
        return 0;
    }

    private void spawnSingle(ObstacleType type) {
        Obstacle obstacle = pool.acquire(type, SPAWN_X, 0);
        double maxY = App.HEIGHT - Decor.FLOOR_HEIGHT - obstacle.getHitbox().getHeight();
        obstacle.reset(SPAWN_X, 20 + random.nextDouble() * (maxY - 20));
        obstacles.add(obstacle);
    }

    private void spawnCorridor() {
        double center = 200 + random.nextDouble() * 240;
        Obstacle top = pool.acquire(ObstacleType.ZAPPER_HORIZONTAL_LONG, SPAWN_X, 0);
        top.reset(SPAWN_X, center - CORRIDOR_GAP / 2 - top.getHitbox().getHeight());
        Obstacle bottom = pool.acquire(ObstacleType.ZAPPER_HORIZONTAL_LONG, SPAWN_X, center + CORRIDOR_GAP / 2);
        obstacles.add(top);
        obstacles.add(bottom);
    }

    private void spawnGate() {
        double center = 270 + (random.nextDouble() - 0.5) * 80;
        Obstacle top = pool.acquire(ObstacleType.ZAPPER_VERTICAL_LONG, SPAWN_X, 0);
        double length = top.getHitbox().getHeight();
        top.reset(SPAWN_X, center - GATE_GAP / 2 - length);
        Obstacle bottom = pool.acquire(ObstacleType.ZAPPER_VERTICAL_LONG, SPAWN_X, center + GATE_GAP / 2);
        obstacles.add(top);
        obstacles.add(bottom);
    }

    private void checkDouble() {
        boolean due = doublesDone < doublesPerGame && elapsed >= nextDoubleAt;
        if (due && doubleGroup.isEmpty() && laserAllowed()) {
            doubleDue = true;
        }
    }

    private void spawnDoubleLaser() {
        double span = 2 * Laser.THICKNESS + DOUBLE_GAP;
        double floorY = App.HEIGHT - Decor.FLOOR_HEIGHT;
        double top = 20 + random.nextDouble() * (floorY - 70 - span - 20);
        for (int i = 0; i < 2; i++) {
            Obstacle laser = pool.acquire(ObstacleType.LASER, SPAWN_X, 0);
            laser.reset(SPAWN_X, top + i * (Laser.THICKNESS + DOUBLE_GAP));
            obstacles.add(laser);
            doubleGroup.add(laser);
        }
        doublesDone++;
        nextDoubleAt = elapsed + DOUBLE_COOLDOWN;
        lastLaserAt = elapsed;
        doubleDue = false;
    }

    private void releaseDouble() {
        if (doubleGroup.isEmpty()) {
            return;
        }
        doubleGroup.removeIf(o -> !obstacles.contains(o));
        if (doubleGroup.isEmpty()) {
            spawnTimer = AFTER_DOUBLE_DELAY;
        }
    }

    private void spawnScientists() {
        int count = 1 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            double walk = -70 + random.nextDouble() * 180;
            double depth = random.nextDouble() * 10;
            scientists.add(new Scientist(SPAWN_X + i * 45, walk, depth, speed));
        }
    }

    private void updateScientists(double dt) {
        scientistTimer -= dt;
        if (scientistTimer <= 0) {
            spawnScientists();
            scientistTimer = SCIENTIST_MIN_DELAY
                + random.nextDouble() * (SCIENTIST_MAX_DELAY - SCIENTIST_MIN_DELAY);
        }
        for (Scientist s : scientists) {
            s.update(dt);
            s.react(player.getHitbox(), player.isThrustPressed());
        }
        scientists.removeIf(Scientist::isOffScreen);
    }

    static double introSpeed(double time) {
        double t = Math.min(1, Math.max(0, time / BURST_TIME));
        double remaining = (1 - t) * (1 - t);
        return WALK_SPEED + (BURST_SPEED - WALK_SPEED) * remaining;
    }

    private void updateIntro(double dt) {
        introTime += dt;
        speed.setSpeed(introSpeed(introTime));
        decor.update(dt);
        wall.update(dt);
        if (wall.isStanding() && wall.touches(player.getHitbox())) {
            wall.shatter(random);
            player.enter();
        }
        if (decor.getDistance() >= Decor.ROOM_END - PLAYER_FRONT) {
            intro = false;
        }
    }

    private void spawnPowerUp() {
        PowerUpEffect effect = random.nextBoolean() ? new ShieldEffect() : new VehicleEffect();
        double maxY = App.HEIGHT - Decor.FLOOR_HEIGHT - 40;
        powerUps.add(new PowerUp(SPAWN_X, 60 + random.nextDouble() * (maxY - 60), speed, effect));
    }

    private void updatePowerUps(double dt) {
        powerUpTimer -= dt;
        if (powerUpTimer <= 0) {
            spawnPowerUp();
            powerUpTimer = (POWERUP_MIN_DELAY + random.nextDouble() * (POWERUP_MAX_DELAY - POWERUP_MIN_DELAY)) * delayScale;
        }
        for (PowerUp p : powerUps) {
            p.update(dt);
            if (!p.isCollected() && p.getHitbox().intersects(player.getHitbox())) {
                p.onCollision(player);
            }
        }
        powerUps.removeIf(p -> p.isCollected() || p.isOffScreen());
    }

    private boolean coinSpaceClear() {
        return obstacles.stream().noneMatch(o -> o.getX() + o.getHitbox().getWidth() > SPAWN_X - COIN_CLEARANCE);
    }

    private void spawnCoins() {
        int count = 3 + random.nextInt(5);
        double maxY = App.HEIGHT - Decor.FLOOR_HEIGHT - Coin.SIZE;
        double y = 60 + random.nextDouble() * (maxY - 60);
        for (int i = 0; i < count; i++) {
            coins.add(new Coin(SPAWN_X + i * COIN_SPACING, y, speed, score));
        }
    }

    private void updateCoins(double dt) {
        coinTimer -= dt;
        if (coinTimer <= 0 && coinSpaceClear()) {
            spawnCoins();
            coinTimer = (COIN_MIN_DELAY + random.nextDouble() * (COIN_MAX_DELAY - COIN_MIN_DELAY)) * delayScale;
        }
        for (Coin c : coins) {
            c.update(dt);
            if (!c.isCollected() && c.getHitbox().intersects(player.getHitbox())) {
                c.onCollision(player);
                events.publish(GameEvent.COIN_COLLECTED);
            }
        }
        coins.removeIf(c -> c.isCollected() || c.isOffScreen());
    }

    @Override
    public void update(double dt) {
        // Mort : plus de pause, le décor reste figé pendant que le corps retombe
        if (player.isDead()) {
            player.update(dt);
            deathTime += dt;
            if (deathTime >= DEATH_DELAY) {
                game.gameOver(this);
            }
            return;
        }
        if (input.isPausePressed()) {
            game.pause(this);
            return;
        }
        player.update(dt);
        if (intro) {
            updateIntro(dt);
            return;
        }
        // Mode Rush : le temps de jeu passe plus vite, donc tout se débloque et accélère plus tôt
        elapsed += rush ? dt * RushMode.TIME_FACTOR : dt;
        speed.setSpeed(Math.min(rush ? RushMode.MAX_SPEED : MAX_SPEED, START_SPEED + elapsed * SPEED_GAIN));
        score.addDistance(speed.getSpeed() * dt);
        decor.update(dt);
        wall.update(dt);
        updateScientists(dt);
        updatePowerUps(dt);
        updateCoins(dt);
        checkDouble();
        if (doubleDue) {
            if (obstacles.isEmpty()) {
                spawnDoubleLaser();
            }
        } else if (doubleGroup.isEmpty()) {
            spawnTimer -= dt;
            if (spawnTimer <= 0) {
                double clearance = spawn();
                spawnTimer = spawnDelay() + clearance / speed.getSpeed();
            }
        }
        for (Obstacle o : obstacles) {
            o.update(dt);
            if (o.collidesWith(player.getHitbox())) {
                o.onCollision(player);
            }
        }
        obstacles.removeIf(o -> {
            if (o.isOffScreen()) {
                pool.release(o);
                return true;
            }
            return false;
        });
        releaseDouble();
    }

    @Override
    public void render(GraphicsContext gc) {
        decor.render(gc);
        wall.render(gc);
        scientists.forEach(s -> s.render(gc));
        obstacles.forEach(o -> o.render(gc));
        powerUps.forEach(p -> p.render(gc));
        coins.forEach(c -> c.render(gc));
        player.render(gc);
        scoreHud.render(gc, 20, 50);
        indicator.render(gc, player, 20, 140);
    }

    void showBest(int best) {
        scoreHud.setBest(best);
    }

    ScoreManager getScore() {
        return score;
    }

    /**
     * Applique au joueur le skin choisi dans le menu.
     *
     * @param path chemin de la feuille de sprites du skin
     */
    void useSkin(String path) {
        player.setSkin(path);
    }

    /**
     * Abonne un observateur aux événements de cette partie (pièce ramassée, choc...).
     *
     * @param listener l'observateur à prévenir
     */
    void addEventListener(GameEventListener listener) {
        events.addListener(listener);
    }

    /** Relance les sons qui tournent en boucle, après une pause. */
    void resumeSounds() {
        player.resumeSounds();
    }

    Player getPlayer() {
        return player;
    }

    boolean isRush() {
        return rush;
    }

    double getElapsed() {
        return elapsed;
    }

    double getScrollSpeed() {
        return speed.getSpeed();
    }

    Decor getDecor() {
        return decor;
    }
}