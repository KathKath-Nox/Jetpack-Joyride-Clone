package com.jetpackrush;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * ÉCRAN « SON » : volume de la musique, musique activée ou non, effets sonores activés ou non.
 *
 * <p>Flèches haut/bas pour choisir une ligne. Gauche/droite règlent le volume ou basculent une
 * option ; Entrée ou Espace basculent une option ou valident « Retour ». Échap revient au menu.
 * Les changements sont appliqués tout de suite, on entend le résultat.
 */
public class SoundState implements GameState {

    // Lignes de l'écran, de haut en bas
    static final int MUSIC_ON = 0;
    static final int MUSIC_VOLUME = 1;
    static final int EFFECTS_ON = 2;
    static final int BACK = 3;
    private static final int ROWS = 4;

    private static final double PANEL_WIDTH = 760;
    private static final double PANEL_Y = 150;
    private static final double TITLE_HEIGHT = 110;
    private static final double ROW_HEIGHT = 60;
    private static final double PANEL_HEIGHT = TITLE_HEIGHT + ROWS * ROW_HEIGHT + 20;
    private static final double MARGIN = 40;

    // Barre du volume : une case par pas
    private static final int SEGMENTS = 100 / SoundSettings.STEP;
    private static final double SEGMENT_WIDTH = 18;
    private static final double SEGMENT_GAP = 4;
    private static final double SEGMENT_HEIGHT = 26;

    private static final Color PANEL = Color.rgb(0, 0, 0, 0.7);
    private static final Color SELECTED = Color.GOLD;
    private static final Color NOT_SELECTED = Color.LIGHTGRAY;
    private static final Color EMPTY = Color.rgb(90, 90, 90);

    private final Game game;
    private final KeyboardInput input;
    private final SoundSettings settings;
    private int selected;

    // Chargés au premier affichage : JavaFX ne peut pas créer d'Image sans fenêtre ouverte
    private Image background;
    private Font font;
    private Font smallFont;

    /**
     * Crée l'écran du son.
     *
     * @param game     le jeu, pour revenir au menu
     * @param input    le clavier
     * @param settings les réglages que l'écran modifie
     */
    public SoundState(Game game, KeyboardInput input, SoundSettings settings) {
        this.game = game;
        this.input = input;
        this.settings = settings;
    }

    private void loadAssets() {
        try (InputStream image = Assets.open("title-bg.png")) {
            background = new Image(image, App.WIDTH, App.HEIGHT, false, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        font = Assets.font(32);
        smallFont = Assets.font(24);
    }

    @Override
    public void update(double dt) {
        if (input.wasPressed(KeyCode.UP)) {
            selected = (selected + ROWS - 1) % ROWS;
        }
        if (input.wasPressed(KeyCode.DOWN)) {
            selected = (selected + 1) % ROWS;
        }
        if (input.wasPressed(KeyCode.LEFT)) {
            adjust(-1);
        }
        if (input.wasPressed(KeyCode.RIGHT)) {
            adjust(1);
        }
        if (input.wasPressed(KeyCode.ESCAPE)) {
            game.backToMenu();
        } else if (input.wasPressed(KeyCode.SPACE) || input.wasPressed(KeyCode.ENTER)) {
            activate();
        }
    }

    // Gauche/droite : règle le volume, ou bascule une option
    private void adjust(int direction) {
        if (selected == MUSIC_VOLUME) {
            settings.changeMusicVolume(direction);
        } else if (selected == MUSIC_ON) {
            settings.setMusicOn(!settings.isMusicOn());
        } else if (selected == EFFECTS_ON) {
            settings.setEffectsOn(!settings.isEffectsOn());
        }
    }

    // Entrée/Espace : bascule une option, ou revient au menu
    private void activate() {
        if (selected == MUSIC_ON) {
            settings.setMusicOn(!settings.isMusicOn());
        } else if (selected == EFFECTS_ON) {
            settings.setEffectsOn(!settings.isEffectsOn());
        } else if (selected == BACK) {
            game.backToMenu();
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (background == null) {
            loadAssets();
        }
        gc.save();
        gc.drawImage(background, 0, 0);

        double centerX = App.WIDTH / 2.0;
        double panelX = centerX - PANEL_WIDTH / 2;
        gc.setFill(PANEL);
        gc.fillRoundRect(panelX, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT, 28, 28);

        gc.setFont(font);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(SELECTED);
        gc.fillText("SON", centerX, PANEL_Y + 55);

        for (int i = 0; i < ROWS; i++) {
            double y = PANEL_Y + TITLE_HEIGHT + i * ROW_HEIGHT;
            Color color = i == selected ? SELECTED : NOT_SELECTED;
            gc.setFill(color);
            gc.setFont(font);
            if (i == BACK) {
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("RETOUR", centerX, y);
                continue;
            }
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(label(i), panelX + MARGIN, y);
            gc.setTextAlign(TextAlignment.RIGHT);
            if (i == MUSIC_VOLUME) {
                drawVolumeBar(gc, panelX + PANEL_WIDTH - MARGIN, y, color);
            } else {
                boolean on = i == MUSIC_ON ? settings.isMusicOn() : settings.isEffectsOn();
                gc.fillText(on ? "OUI" : "NON", panelX + PANEL_WIDTH - MARGIN, y);
            }
        }

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(smallFont);
        gc.setFill(NOT_SELECTED);
        gc.fillText("FLECHES POUR CHOISIR ET REGLER   ENTREE POUR VALIDER   ECHAP POUR RETOUR",
                centerX, App.HEIGHT - 30);
        gc.restore();
    }

    private static String label(int row) {
        return switch (row) {
            case MUSIC_ON -> "MUSIQUE";
            case MUSIC_VOLUME -> "VOLUME MUSIQUE";
            default -> "EFFETS SONORES";
        };
    }

    // Dessine la barre alignée à droite : cases pleines jusqu'au volume, vides ensuite
    private void drawVolumeBar(GraphicsContext gc, double right, double baseline, Color color) {
        int filled = settings.getMusicVolume() / SoundSettings.STEP;
        double width = SEGMENTS * (SEGMENT_WIDTH + SEGMENT_GAP) - SEGMENT_GAP;
        double left = right - width;
        for (int s = 0; s < SEGMENTS; s++) {
            gc.setFill(s < filled ? color : EMPTY);
            gc.fillRect(left + s * (SEGMENT_WIDTH + SEGMENT_GAP), baseline - SEGMENT_HEIGHT,
                    SEGMENT_WIDTH, SEGMENT_HEIGHT);
        }
    }

    int getSelected() {
        return selected;
    }
}
