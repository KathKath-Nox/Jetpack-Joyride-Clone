package com.jetpackrush;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * FIN DE PARTIE : écran affiché par-dessus la partie figée quand le joueur meurt.
 *
 * <p>« Rejouer » relance une nouvelle partie. « Quitter » (ou Échap) revient au menu principal.
 */
public class GameOverState implements GameState {

    // Options de fin de partie, dans l'ordre d'affichage (de haut en bas)
    static final int RETRY = 0;
    static final int QUIT = 1;
    private static final String[] OPTIONS = {"REJOUER", "QUITTER"};

    private static final double TITLE_Y = 250;
    private static final double SCORE_Y = 315;
    private static final double BEST_Y = 358;
    private static final double OPTIONS_Y = 420;
    private static final double OPTION_SPACING = 70;

    private static final Color VEIL = Color.rgb(0, 0, 0, 0.6);
    private static final Color TITLE = Color.TOMATO;
    private static final Color SELECTED = Color.GOLD;
    private static final Color NOT_SELECTED = Color.LIGHTGRAY;

    private final Game game;
    private final KeyboardInput input;
    private final PlayState play;
    private int selected = RETRY;

    // Chargées au premier affichage, comme dans MenuState, pour que les tests tournent sans fenêtre
    private Font titleFont;
    private Font optionFont;

    /**
     * Crée l'écran de fin de partie.
     *
     * @param game  le jeu, pour relancer une partie ou revenir au menu
     * @param input le clavier
     * @param play  la partie terminée, affichée figée en fond
     */
    public GameOverState(Game game, KeyboardInput input, PlayState play) {
        this.game = game;
        this.input = input;
        this.play = play;
    }

    private void loadFonts() {
        titleFont = Assets.font(72);
        optionFont = Assets.font(32);
    }

    @Override
    public void update(double dt) {
        if (input.wasPressed(KeyCode.UP) || input.wasPressed(KeyCode.LEFT)) {
            selected = (selected + OPTIONS.length - 1) % OPTIONS.length;
        }
        if (input.wasPressed(KeyCode.DOWN) || input.wasPressed(KeyCode.RIGHT)) {
            selected = (selected + 1) % OPTIONS.length;
        }
        // Échap : même effet que « Quitter »
        if (input.wasPressed(KeyCode.ESCAPE)) {
            game.backToMenu();
        } else if (input.wasPressed(KeyCode.SPACE) || input.wasPressed(KeyCode.ENTER)) {
            if (selected == RETRY) {
                game.startNewGame();
            } else {
                game.backToMenu();
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (titleFont == null) {
            loadFonts();
        }
        // La partie reste affichée en fond, figée à l'instant de la mort
        play.render(gc);

        gc.save();
        gc.setFill(VEIL);
        gc.fillRect(0, 0, App.WIDTH, App.HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        double centerX = App.WIDTH / 2.0;
        gc.setFont(titleFont);
        gc.setFill(TITLE);
        gc.fillText("GAME OVER", centerX, TITLE_Y);

        gc.setFont(optionFont);
        gc.setFill(Color.WHITE);
        gc.fillText(play.getScore().getDistance() + " M   -   " + play.getScore().getCoins() + " PIECES",
            centerX, SCORE_Y);

        BestScore best = game.getBestScore();
        if (best.isNewRecord()) {
            gc.setFill(SELECTED);
            gc.fillText("NOUVEAU RECORD !", centerX, BEST_Y);
        } else {
            gc.setFill(NOT_SELECTED);
            gc.fillText("MEILLEUR : " + best.get() + " M", centerX, BEST_Y);
        }

        // Options de fin de partie, celle sélectionnée en doré
        gc.setFont(optionFont);
        for (int i = 0; i < OPTIONS.length; i++) {
            gc.setFill(i == selected ? SELECTED : NOT_SELECTED);
            gc.fillText(OPTIONS[i], centerX, OPTIONS_Y + i * OPTION_SPACING);
        }
        gc.restore();
    }

    int getSelected() {
        return selected;
    }
}
