package com.jetpackrush;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;

import com.jetpackrush.KeyBindings.Action;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * ÉCRAN « TOUCHES » : permet au joueur de changer les touches du jeu.
 *
 * <p>Flèches haut/bas pour choisir une ligne, Entrée ou Espace pour la valider, Échap pour revenir
 * au menu. Sur une action (Jetpack, Pause), la validation passe en écoute : la prochaine touche
 * appuyée devient la nouvelle commande. Une touche déjà utilisée ou réservée (Entrée, flèches, F11)
 * est refusée avec un message. « Son » ouvre les réglages du son. « Réinitialiser » remet les touches par défaut.
 */
public class ControlsState implements GameState {

    private static final Action[] ACTIONS = Action.values();

    // Lignes de l'écran : une par action, puis « Son », « Réinitialiser » et « Retour »
    static final int SOUND = ACTIONS.length;
    static final int RESET = ACTIONS.length + 1;
    static final int BACK = ACTIONS.length + 2;
    private static final int ROWS = ACTIONS.length + 3;

    private static final double PANEL_WIDTH = 760;
    private static final double PANEL_Y = 150;
    private static final double TITLE_HEIGHT = 110;
    private static final double ROW_HEIGHT = 60;
    private static final double PANEL_HEIGHT = TITLE_HEIGHT + ROWS * ROW_HEIGHT + 20;
    private static final double MARGIN = 40;

    private static final Color PANEL = Color.rgb(0, 0, 0, 0.7);
    private static final Color SELECTED = Color.GOLD;
    private static final Color NOT_SELECTED = Color.LIGHTGRAY;
    private static final Color ERROR = Color.ORANGERED;

    private final Game game;
    private final KeyboardInput input;
    private final KeyBindings bindings;
    private int selected;
    private boolean listening;
    private String message = "";

    // Chargés au premier affichage : JavaFX ne peut pas créer d'Image sans fenêtre ouverte
    private Image background;
    private Font font;
    private Font smallFont;

    /**
     * Crée l'écran des touches.
     *
     * @param game  le jeu, pour revenir au menu
     * @param input le clavier, qui contient la configuration des touches
     */
    public ControlsState(Game game, KeyboardInput input) {
        this.game = game;
        this.input = input;
        this.bindings = input.getBindings();
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
        if (listening) {
            listen();
            return;
        }
        if (input.wasPressed(KeyCode.UP)) {
            selected = (selected + ROWS - 1) % ROWS;
            message = "";
        }
        if (input.wasPressed(KeyCode.DOWN)) {
            selected = (selected + 1) % ROWS;
            message = "";
        }
        if (input.wasPressed(KeyCode.ESCAPE)) {
            game.backToMenu();
        } else if (input.wasPressed(KeyCode.SPACE) || input.wasPressed(KeyCode.ENTER)) {
            activate();
        }
    }

    // Valide la ligne choisie : écoute une touche, remet les réglages par défaut ou revient au menu
    private void activate() {
        if (selected < ACTIONS.length) {
            listening = true;
            message = "";
        } else if (selected == SOUND) {
            game.showSound();
        } else if (selected == RESET) {
            bindings.reset();
            message = "";
        } else {
            game.backToMenu();
        }
    }

    // Attend la prochaine touche et l'assigne à l'action choisie, sauf conflit ou touche réservée
    private void listen() {
        Optional<KeyCode> pressed = input.anyJustPressed();
        if (pressed.isEmpty()) {
            return;
        }
        listening = false;
        Action action = ACTIONS[selected];
        KeyCode key = pressed.get();
        if (bindings.bind(action, key)) {
            message = "";
        } else if (bindings.isReserved(key)) {
            message = "TOUCHE RESERVEE";
        } else {
            String owner = bindings.conflictWith(action, key)
                    .map(other -> other.getLabel().toUpperCase())
                    .orElse("");
            message = "DEJA UTILISEE PAR " + owner;
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
        gc.fillText("TOUCHES", centerX, PANEL_Y + 55);

        for (int i = 0; i < ROWS; i++) {
            double y = PANEL_Y + TITLE_HEIGHT + i * ROW_HEIGHT;
            gc.setFill(i == selected ? SELECTED : NOT_SELECTED);
            if (i < ACTIONS.length) {
                gc.setTextAlign(TextAlignment.LEFT);
                gc.fillText(ACTIONS[i].getLabel().toUpperCase(), panelX + MARGIN, y);
                gc.setTextAlign(TextAlignment.RIGHT);
                String key = listening && i == selected
                        ? "APPUYEZ SUR UNE TOUCHE"
                        : bindings.get(ACTIONS[i]).getName().toUpperCase();
                gc.fillText(key, panelX + PANEL_WIDTH - MARGIN, y);
            } else {
                gc.setTextAlign(TextAlignment.CENTER);
                String label = i == SOUND ? "SON" : i == RESET ? "REINITIALISER" : "RETOUR";
                gc.fillText(label, centerX, y);
            }
        }

        gc.setTextAlign(TextAlignment.CENTER);
        if (!message.isEmpty()) {
            gc.setFill(ERROR);
            gc.fillText(message, centerX, PANEL_Y + PANEL_HEIGHT + 50);
        }
        gc.setFont(smallFont);
        gc.setFill(NOT_SELECTED);
        gc.fillText("FLECHES POUR CHOISIR   ENTREE POUR VALIDER   ECHAP POUR RETOUR",
                centerX, App.HEIGHT - 30);
        gc.restore();
    }

    int getSelected() {
        return selected;
    }

    boolean isListening() {
        return listening;
    }

    String getMessage() {
        return message;
    }
}