package com.jetpackrush;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * PAUSE : écran affiché par-dessus la partie figée quand le joueur appuie sur Échap.
 *
 * <p>« Reprendre » (ou Échap) lance un compte à rebours de 3 secondes avant de relancer
 * la partie, pour laisser au joueur le temps de se préparer ; Échap pendant le compte à
 * rebours revient au menu de pause. « Quitter » abandonne la partie et revient au menu principal.
 */
public class PauseState implements GameState {

    // Options de la pause, dans l'ordre d'affichage (de haut en bas)
    static final int RESUME = 0;
    static final int QUIT = 1;
    private static final String[] OPTIONS = {"REPRENDRE", "QUITTER"};

    /** Durée du compte à rebours avant la reprise, en secondes. */
    static final double COUNTDOWN_DURATION = 3;

    private static final double TITLE_Y = 250;
    private static final double OPTIONS_Y = 380;
    private static final double OPTION_SPACING = 70;

    // Voile sombre sur la partie figée, plus léger pendant le compte à rebours
    // pour que le joueur voie où il en est avant de reprendre
    private static final Color MENU_VEIL = Color.rgb(0, 0, 0, 0.6);
    private static final Color COUNTDOWN_VEIL = Color.rgb(0, 0, 0, 0.3);
    private static final Color SELECTED = Color.GOLD;
    private static final Color NOT_SELECTED = Color.LIGHTGRAY;

    private final Game game;
    private final KeyboardInput input;
    private final PlayState play;
    private int selected = RESUME;
    private boolean countingDown;
    private double remaining;

    // Chargées au premier affichage, comme dans MenuState, pour que les tests tournent sans fenêtre
    private Font titleFont;
    private Font optionFont;
    private Font countdownFont;

    /**
     * Crée l'écran de pause.
     *
     * @param game  le jeu, pour reprendre la partie ou revenir au menu
     * @param input le clavier
     * @param play  la partie figée, affichée en fond et reprise telle quelle
     */
    public PauseState(Game game, KeyboardInput input, PlayState play) {
        this.game = game;
        this.input = input;
        this.play = play;
    }

    private void loadFonts() {
        titleFont = Assets.font(72);
        optionFont = Assets.font(32);
        countdownFont = Assets.font(160);
    }

    @Override
    public void update(double dt) {
        if (countingDown) {
            updateCountdown(dt);
        } else {
            updateMenu();
        }
    }

    private void updateMenu() {
        if (input.wasPressed(KeyCode.UP) || input.wasPressed(KeyCode.LEFT)) {
            selected = (selected + OPTIONS.length - 1) % OPTIONS.length;
        }
        if (input.wasPressed(KeyCode.DOWN) || input.wasPressed(KeyCode.RIGHT)) {
            selected = (selected + 1) % OPTIONS.length;
        }
        // Échap une seconde fois : même effet que « Reprendre »
        if (input.wasPressed(KeyCode.ESCAPE)) {
            startCountdown();
        } else if (input.wasPressed(KeyCode.SPACE) || input.wasPressed(KeyCode.ENTER)) {
            if (selected == RESUME) {
                startCountdown();
            } else {
                game.backToMenu();
            }
        }
    }

    private void startCountdown() {
        countingDown = true;
        remaining = COUNTDOWN_DURATION;
    }

    private void updateCountdown(double dt) {
        // Échap pendant le compte à rebours : le joueur n'est pas prêt, retour au menu de pause
        if (input.wasPressed(KeyCode.ESCAPE)) {
            countingDown = false;
            return;
        }
        remaining -= dt;
        if (remaining <= 0) {
            game.resume(play);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (titleFont == null) {
            loadFonts();
        }
        // La partie reste affichée en fond, figée à l'instant de la pause
        play.render(gc);

        gc.save();
        gc.setFill(countingDown ? COUNTDOWN_VEIL : MENU_VEIL);
        gc.fillRect(0, 0, App.WIDTH, App.HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        if (countingDown) {
            renderCountdown(gc);
        } else {
            renderMenu(gc);
        }
        gc.restore();
    }

    private void renderMenu(GraphicsContext gc) {
        double centerX = App.WIDTH / 2.0;
        gc.setFont(titleFont);
        gc.setFill(Color.WHITE);
        gc.fillText("PAUSE", centerX, TITLE_Y);

        // Options de la pause, celle sélectionnée en doré
        gc.setFont(optionFont);
        for (int i = 0; i < OPTIONS.length; i++) {
            gc.setFill(i == selected ? SELECTED : NOT_SELECTED);
            gc.fillText(OPTIONS[i], centerX, OPTIONS_Y + i * OPTION_SPACING);
        }
    }

    private void renderCountdown(GraphicsContext gc) {
        // 3, 2, 1 : chaque chiffre reste affiché une seconde
        String seconds = String.valueOf((int) Math.ceil(remaining));
        double centerX = App.WIDTH / 2.0;
        double centerY = App.HEIGHT / 2.0;
        gc.setFont(countdownFont);
        // Contour noir pour que le chiffre reste lisible sur le ciel clair
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(8);
        gc.strokeText(seconds, centerX, centerY);
        gc.setFill(SELECTED);
        gc.fillText(seconds, centerX, centerY);
    }

    int getSelected() {
        return selected;
    }

    boolean isCountingDown() {
        return countingDown;
    }
}
