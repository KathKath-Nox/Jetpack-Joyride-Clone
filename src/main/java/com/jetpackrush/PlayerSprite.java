package com.jetpackrush;

import java.util.Objects;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Dessine le sprite animé du joueur. La feuille {@code player_sprite_sheet.png} est une grille de
 * 4 colonnes et 6 lignes : chaque état a sa ligne, et la colonne change 12 fois par seconde.
 */
public class PlayerSprite {
    static final String PATH = "/assets/player_sprite_sheet.png";
    private static final int COLUMNS = 4;
    private static final int ROWS = 6;
    private static final double FRAMES_PER_SECOND = 12;
    private static final double SIZE = 90;

    private Image sheet;
    private String sheetPath;

    /**
     * Dessine le joueur avec le skin d'origine.
     *
     * @param gc    contexte graphique du canvas
     * @param state état courant du joueur
     * @param time  temps d'animation écoulé, en secondes
     * @param box   hitbox du joueur
     */
    public void render(GraphicsContext gc, PlayerState state, double time, Hitbox box) {
        render(gc, PATH, state, time, box);
    }

    /**
     * Dessine l'image du joueur qui correspond à son état et au temps écoulé. L'image de la feuille
     * est chargée au premier appel. Le sprite fait 90 par 90 pixels, centré sur la hitbox.
     *
     * @param gc    contexte graphique du canvas
     * @param path  chemin de la feuille de sprites du skin choisi
     * @param state état courant du joueur, qui choisit la ligne de la feuille
     * @param time  temps d'animation écoulé, en secondes
     * @param box   hitbox du joueur, qui donne la position du sprite
     */
    public void render(GraphicsContext gc, String path, PlayerState state, double time, Hitbox box) {
        if (sheet == null || !path.equals(sheetPath)) {
            sheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            sheetPath = path;
        }
        double frameWidth = sheet.getWidth() / COLUMNS;
        double frameHeight = sheet.getHeight() / ROWS;
        int column = (int) (time * FRAMES_PER_SECOND) % COLUMNS;
        int row = rowFor(state);

        double drawX = box.getX() + box.getWidth() / 2 - SIZE / 2;
        double drawY = box.getY() + box.getHeight() - SIZE + 8;
        gc.drawImage(sheet, column * frameWidth, row * frameHeight, frameWidth, frameHeight,
                drawX, drawY, SIZE, SIZE);
    }

    private int rowFor(PlayerState state) {
        if (state instanceof Flying) {
            return 1;
        }
        if (state instanceof Dead) {
            return 3;
        }
        return 2;
    }
}
