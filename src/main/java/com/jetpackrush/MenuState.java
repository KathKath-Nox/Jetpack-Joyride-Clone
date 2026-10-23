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
 * MENU PRINCIPAL : écran affiché avant d'entrer dans le jeu.
 *
 * <p>Fond : {@code assets/title-bg.png}, qui contient déjà le titre du jeu.
 * Flèches pour choisir, Espace ou Entrée pour valider, Échap pour quitter.
 * L'option « Réglages » ouvre l'écran qui permet de changer les touches, l'option « Customiser »
 * ouvre l'écran de choix du personnage et « Rush » lance le mode démo (partie accélérée avec
 * tous les obstacles).
 */
public class MenuState implements GameState {

    // Options du menu, dans l'ordre d'affichage (de gauche à droite)
    static final int PLAY = 0;
    static final int QUIT = 1;
    static final int SETTINGS = 2;
    static final int SKIN = 3;
    static final int RUSH = 4;
    private static final int OPTION_COUNT = 5;

    // Bandeau des options, en bas de l'écran sous « PRESS SPACE TO BEGIN »
    private static final double BAR_WIDTH = 1140;
    private static final double BAR_HEIGHT = 56;
    private static final double BAR_Y = App.HEIGHT - BAR_HEIGHT - 12;
    private static final double OPTION_SPACING = 215;

    // QR code de la manette téléphone, en haut à droite
    private static final double QR_SIZE = 150;
    private static final double QR_QUIET = 10;

    private static final Color BAR = Color.rgb(0, 0, 0, 0.6);
    private static final Color SELECTED = Color.GOLD;
    private static final Color NOT_SELECTED = Color.LIGHTGRAY;

    private final Game game;
    private final KeyboardInput input;
    private int selected = PLAY;

    // Chargés au premier affichage et pas dans le constructeur : JavaFX ne peut pas
    // créer d'Image sans fenêtre ouverte, ce qui casserait les tests du menu
    private Image background;
    private Font font;
    private Font smallFont;
    private String qrUrl;
    private boolean[][] qr;

    /**
     * Crée le menu principal.
     *
     * @param game  le jeu, pour lancer une partie ou quitter
     * @param input le clavier
     */
    public MenuState(Game game, KeyboardInput input) {
        this.game = game;
        this.input = input;
    }

    private void loadAssets() {
        try (InputStream image = Assets.open("title-bg.png")) {
            // Image redimensionnée une seule fois au chargement (1920x1080 -> taille de la fenêtre)
            background = new Image(image, App.WIDTH, App.HEIGHT, false, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        font = Assets.font(32);
        smallFont = Assets.font(18);
    }

    private void renderPhoneQr(GraphicsContext gc) {
        String url = game.getPhoneUrl();
        if (url == null) {
            return;
        }
        if (!url.equals(qrUrl)) {
            qr = QrCode.encode(url);
            qrUrl = url;
        }
        double cell = Math.floor(QR_SIZE / qr.length);
        double size = cell * qr.length;
        double x = App.WIDTH - size - QR_QUIET - 20;
        double y = 20;
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(x - QR_QUIET, y - QR_QUIET, size + 2 * QR_QUIET, size + 2 * QR_QUIET, 12, 12);
        gc.setFill(Color.BLACK);
        for (int row = 0; row < qr.length; row++) {
            for (int col = 0; col < qr.length; col++) {
                if (qr[row][col]) {
                    gc.fillRect(x + col * cell, y + row * cell, cell, cell);
                }
            }
        }
        gc.setFont(smallFont);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("JOUE AVEC TON TELEPHONE", x + size / 2, y + size + QR_QUIET + 26);
    }

    @Override
    public void update(double dt) {
        if (input.wasPressed(KeyCode.LEFT) || input.wasPressed(KeyCode.UP)) {
            selected = (selected + OPTION_COUNT - 1) % OPTION_COUNT;
        }
        if (input.wasPressed(KeyCode.RIGHT) || input.wasPressed(KeyCode.DOWN)) {
            selected = (selected + 1) % OPTION_COUNT;
        }
        if (input.wasPressed(KeyCode.ESCAPE)) {
            game.quit();
        } else if (input.wasPressed(KeyCode.SPACE) || input.wasPressed(KeyCode.ENTER)) {
            if (selected == PLAY) {
                game.startNewGame();
            } else if (selected == SETTINGS) {
                game.showControls();
            } else if (selected == SKIN) {
                game.showCustomize();
            } else if (selected == RUSH) {
                game.startRushGame();
            } else {
                game.quit();
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (background == null) {
            loadAssets();
        }
        gc.save();
        gc.drawImage(background, 0, 0);

        // Bandeau semi-transparent pour que les options restent lisibles sur l'image
        double centerX = App.WIDTH / 2.0;
        gc.setFill(BAR);
        gc.fillRoundRect(centerX - BAR_WIDTH / 2, BAR_Y, BAR_WIDTH, BAR_HEIGHT, 28, 28);

        // Options du menu, celle sélectionnée en doré
        // (pas de flèches « > < » : la police du jeu n'a pas ces caractères)
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.CENTER);
        double textY = BAR_Y + BAR_HEIGHT / 2 + 11;
        String[] labels = {"JOUER", "QUITTER", "REGLAGES", "CUSTOMISER", "RUSH"};
        for (int i = 0; i < OPTION_COUNT; i++) {
            double x = centerX + (i - (OPTION_COUNT - 1) / 2.0) * OPTION_SPACING;
            gc.setFill(i == selected ? SELECTED : NOT_SELECTED);
            gc.fillText(labels[i], x, textY);
        }
        renderPhoneQr(gc);
        gc.restore();
    }

    int getSelected() {
        return selected;
    }
}