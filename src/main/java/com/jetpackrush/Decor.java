package com.jetpackrush;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Décor défilant. Dans l'ordre : le couloir de départ, le mur d'entrée (géré à part par IntroWall),
 * la salle du début, puis une suite de décors (couloir métallique, labo, marécage, décharge)
 * qui se remplacent toutes les {@code ZONE_LENGTH} pixels avec un fondu enchaîné.
 */
public class Decor {

    /** Hauteur du sol en pixels (le sol occupe le bas de la fenêtre). */
    public static final double FLOOR_HEIGHT = 80;
    /** Largeur de la salle du début, en pixels. */
    public static final double ROOM_WIDTH = 1049;
    /** Position du mur d'entrée à l'écran au départ : juste devant le joueur. */
    public static final double ENTRY_X = 238;
    /** Position, dans le monde, de la fin de la salle. */
    public static final double ROOM_END = ENTRY_X + ROOM_WIDTH;

    /** Les décors qui se succèdent, dans l'ordre. */
    enum Kind {
        METAL("background2.png"),
        LAB("background.png"),
        SWAMP("swamp_double.png"),
        JUNKYARD("junkyard_double.png");

        private final String image;

        Kind(String image) {
            this.image = image;
        }

        String image() {
            return image;
        }
    }

    static final double STRIPE_WIDTH = 160;
    static final double PATTERN_WIDTH = 2 * STRIPE_WIDTH;
    static final double ZONE_LENGTH = 21000;
    static final double TRANSITION = 100;
    static final double ROOM_FADE = 60;
    static final double CORRIDOR_START = ROOM_END - ROOM_FADE;

    private static final double SLICE = 12;

    private static final String ROOM = "intro_room.png";
    private static final double LAB_SOURCE_Y = 40;
    private static final double LAB_SOURCE_HEIGHT = 260;
    private static final double LAB_SCALE = App.HEIGHT / LAB_SOURCE_HEIGHT;

    private static final Color ROOM_FALLBACK = Color.web("#10182e");
    private static final Color SKY_LIGHT = Color.web("#8ec5e8");
    private static final Color SKY_DARK = Color.web("#7fb9de");
    private static final Color FLOOR_LIGHT = Color.web("#6b4f3a");
    private static final Color FLOOR_DARK = Color.web("#5a412f");

    private final GameSpeed speed;
    private final double zoneLength;
    private double offset;
    private double distance;
    /** Décor du labo agrandi une seule fois, pour ne pas le redimensionner à chaque frame. */
    private Image labScaled;
    private int labCropY;
    private boolean labPrepared;

    /**
     * Crée le décor, au départ de la partie.
     *
     * @param speed source de la vitesse de défilement
     */
    public Decor(GameSpeed speed) {
        this(speed, ZONE_LENGTH);
    }

    /**
     * Crée le décor avec une longueur de zone choisie (le mode Rush utilise des zones courtes).
     *
     * @param speed      source de la vitesse de défilement
     * @param zoneLength longueur d'un décor avant le suivant, en pixels
     * @throws IllegalArgumentException si la longueur n'est pas strictement positive
     */
    public Decor(GameSpeed speed, double zoneLength) {
        if (zoneLength <= 0) {
            throw new IllegalArgumentException("La longueur d'une zone doit être positive");
        }
        this.speed = speed;
        this.zoneLength = zoneLength;
    }

    /**
     * Donne la longueur d'une zone de décor.
     *
     * @return la longueur d'un décor avant le suivant, en pixels
     */
    public double getZoneLength() {
        return zoneLength;
    }

    /**
     * Donne le décor d'une zone : ils se suivent dans l'ordre et on recommence à la fin.
     *
     * @param zone numéro de la zone
     * @return le décor de cette zone
     */
    static Kind kindOf(int zone) {
        Kind[] kinds = Kind.values();
        return kinds[Math.floorMod(zone, kinds.length)];
    }

    /**
     * Courbe douce du fondu entre deux décors.
     *
     * @param t avancement du fondu, entre 0 (début) et 1 (fin) ; les valeurs hors de cet intervalle sont ramenées dedans
     * @return l'opacité du nouveau décor, entre 0 et 1
     */
    static double blend(double t) {
        double c = Math.min(1, Math.max(0, t));
        return c * c * (3 - 2 * c);
    }

    /**
     * Fait avancer le défilement.
     *
     * @param dt temps écoulé depuis la frame précédente, en secondes
     */
    public void update(double dt) {
        double step = speed.getSpeed() * dt;
        offset = (offset + step) % PATTERN_WIDTH;
        distance += step;
    }

    /** Remet le décor au départ de la partie. */
    public void reset() {
        offset = 0;
        distance = 0;
    }

    /**
     * Donne le décalage du motif de secours (les bandes colorées).
     *
     * @return le décalage en pixels
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Donne la distance parcourue depuis le départ.
     *
     * @return la distance en pixels
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Indique si la salle du début n'est pas encore entièrement passée.
     *
     * @return vrai tant que la fin de la salle n'a pas quitté l'écran par la gauche
     */
    public boolean isInRoom() {
        return distance < ROOM_END;
    }

    /**
     * Donne le numéro de la zone où l'on se trouve (0 pour le couloir métallique, 1 pour le labo,
     * 2 pour le marécage, 3 pour la décharge, puis ça recommence). Avant le couloir, c'est 0.
     *
     * @return le numéro de zone
     */
    public int getZone() {
        return (int) Math.floor(Math.max(0, distance - CORRIDOR_START) / zoneLength);
    }

    /**
     * Dessine le décor.
     *
     * @param gc contexte graphique du canvas
     */
    public void render(GraphicsContext gc) {
        prepareLab();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, App.WIDTH, App.HEIGHT);
        renderEntryCorridor(gc);
        int first = getZone();
        for (int zone = Math.max(0, first - 1); zoneStart(zone) - TRANSITION / 2 < App.WIDTH; zone++) {
            renderZone(gc, zone);
        }
        renderRoom(gc);
    }

    /**
     * Agrandit l'image du labo une seule fois, à la première frame. Ensuite elle est dessinée
     * à taille réelle, ce qui est bien plus léger que de l'agrandir à chaque frame.
     * Si quelque chose échoue, on garde l'ancien dessin (agrandi à chaque frame).
     */
    private void prepareLab() {
        if (labPrepared) {
            return;
        }
        labPrepared = true;
        Image source = Sprites.get(Kind.LAB.image());
        if (source == null) {
            return;
        }
        double width = Math.ceil(source.getWidth() * LAB_SCALE) + 1;
        double height = Math.round(source.getHeight() * LAB_SCALE);
        try (InputStream in = Decor.class.getResourceAsStream("/assets/" + Kind.LAB.image())) {
            if (in == null) {
                return;
            }
            Image scaled = new Image(in, width, height, false, true);
            int cropY = (int) Math.round(LAB_SOURCE_Y * LAB_SCALE);
            if (scaled.isError() || cropY + App.HEIGHT > scaled.getHeight()) {
                return;
            }
            labScaled = scaled;
            labCropY = cropY;
        } catch (IOException | RuntimeException e) {
            labScaled = null;
        }
    }

    private double zoneStart(int zone) {
        return CORRIDOR_START + zone * zoneLength - distance;
    }

    private void renderEntryCorridor(GraphicsContext gc) {
        double edge = Math.floor(ENTRY_X - distance);
        if (edge <= 0) {
            return;
        }
        Image image = Sprites.get(Kind.METAL.image());
        gc.save();
        gc.beginPath();
        gc.rect(0, 0, edge, App.HEIGHT);
        gc.clip();
        if (image == null) {
            renderStripes(gc);
        } else {
            drawTile(gc, image, edge - image.getWidth(), image.getWidth(), false, false);
        }
        gc.restore();
    }

    private void renderRoom(GraphicsContext gc) {
        double x = Math.floor(ENTRY_X - distance);
        if (x >= App.WIDTH || x + ROOM_WIDTH <= 0) {
            return;
        }
        Image room = Sprites.get(ROOM);
        if (room == null) {
            gc.setFill(ROOM_FALLBACK);
            gc.fillRect(x, 0, ROOM_WIDTH, App.HEIGHT - FLOOR_HEIGHT);
            return;
        }
        gc.drawImage(room, x, 0);
    }

    private void renderZone(GraphicsContext gc, int zone) {
        double half = TRANSITION / 2;
        double start = Math.floor(zoneStart(zone));
        double end = Math.floor(zoneStart(zone + 1));
        double left = zone == 0 ? Math.max(0, start) : Math.max(0, start - half);
        double right = Math.min(App.WIDTH, end + half);
        if (right <= left) {
            return;
        }
        Kind kind = kindOf(zone);
        Image image = Sprites.get(kind.image());
        if (zone == 0) {
            drawKind(gc, kind, image, start, left, right, 1);
            return;
        }
        double fullFrom = Math.min(right, start + half);
        for (double x = left; x < fullFrom; x += SLICE) {
            double t = (x + SLICE / 2 - (start - half)) / TRANSITION;
            drawKind(gc, kind, image, start, x, Math.min(fullFrom, x + SLICE), blend(t));
        }
        if (fullFrom < right) {
            drawKind(gc, kind, image, start, fullFrom, right, 1);
        }
    }

    private void drawKind(GraphicsContext gc, Kind kind, Image image, double origin,
                          double left, double right, double alpha) {
        gc.save();
        gc.beginPath();
        gc.rect(left, 0, right - left, App.HEIGHT);
        gc.clip();
        gc.setGlobalAlpha(alpha);
        if (image == null) {
            renderStripes(gc);
        } else {
            drawTiles(gc, kind, image, origin);
        }
        gc.restore();
    }

    private void drawTiles(GraphicsContext gc, Kind kind, Image image, double origin) {
        boolean lab = kind == Kind.LAB;
        boolean alternate = kind == Kind.METAL || lab;
        double tileWidth = lab ? image.getWidth() * LAB_SCALE : image.getWidth();
        int first = (int) Math.floor(-origin / tileWidth);
        for (int k = first; origin + k * tileWidth < App.WIDTH; k++) {
            double x = Math.floor(origin + k * tileWidth);
            drawTile(gc, image, x, tileWidth, lab, alternate && Math.floorMod(k, 2) == 1);
        }
    }

    private void drawTile(GraphicsContext gc, Image image, double x, double tileWidth, boolean lab, boolean flipped) {
        double width = lab ? Math.ceil(tileWidth) + 1 : tileWidth;
        gc.save();
        if (flipped) {
            gc.translate(x + width, 0);
            gc.scale(-1, 1);
        } else {
            gc.translate(x, 0);
        }
        if (lab && labScaled != null) {
            gc.drawImage(labScaled, 0, labCropY, labScaled.getWidth(), App.HEIGHT,
                0, 0, labScaled.getWidth(), App.HEIGHT);
        } else if (lab) {
            gc.drawImage(image, 0, LAB_SOURCE_Y, image.getWidth(), LAB_SOURCE_HEIGHT,
                0, 0, width, App.HEIGHT);
        } else {
            gc.drawImage(image, 0, 0);
        }
        gc.restore();
    }

    private void renderStripes(GraphicsContext gc) {
        double floorY = App.HEIGHT - FLOOR_HEIGHT;
        int stripes = (int) Math.ceil(App.WIDTH / STRIPE_WIDTH) + 2;
        for (int i = 0; i < stripes; i++) {
            double x = i * STRIPE_WIDTH - offset;
            boolean even = i % 2 == 0;
            gc.setFill(even ? SKY_LIGHT : SKY_DARK);
            gc.fillRect(x, 0, STRIPE_WIDTH, floorY);
            gc.setFill(even ? FLOOR_LIGHT : FLOOR_DARK);
            gc.fillRect(x, floorY, STRIPE_WIDTH, FLOOR_HEIGHT);
        }
    }
}