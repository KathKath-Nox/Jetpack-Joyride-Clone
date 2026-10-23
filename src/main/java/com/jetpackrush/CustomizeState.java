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
 * ÉCRAN « CUSTOMISER » : le joueur choisit le skin de son personnage.
 *
 * <p>Le personnage est affiché en grand au centre, immobile. Flèches gauche/droite (ou haut/bas)
 * pour changer de skin, Entrée, Espace ou Échap pour revenir au menu. Le choix est gardé par
 * {@link Game} dans {@link CharacterChoice} et appliqué à la partie suivante.
 */
public class CustomizeState implements GameState {

    private static final double PANEL_WIDTH = 760;
    private static final double PANEL_HEIGHT = 500;
    private static final double PANEL_Y = 110;
    private static final double SPRITE_SCALE = 3;
    private static final double SPRITE_BOTTOM = PANEL_Y + 400;
    private static final double ARROW_OFFSET = 230;
    private static final double ARROW_SIZE = 26;

    private static final Color PANEL = Color.rgb(0, 0, 0, 0.85);
    private static final Color GOLD = Color.GOLD;
    private static final Color TEXT = Color.LIGHTGRAY;

    private final Game game;
    private final KeyboardInput input;
    private final CharacterChoice choice;
    private final PlayerSprite sprite = new PlayerSprite();
    private final PlayerState pose = new Falling();

    private Image background;
    private Font font;
    private Font smallFont;

    /**
     * Crée l'écran de choix du personnage.
     *
     * @param game  le jeu, qui garde le skin choisi et permet de revenir au menu
     * @param input le clavier
     */
    public CustomizeState(Game game, KeyboardInput input) {
        this.game = game;
        this.input = input;
        this.choice = game.getCharacterChoice();
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
        if (input.wasPressed(KeyCode.LEFT) || input.wasPressed(KeyCode.UP)) {
            choice.previous();
        }
        if (input.wasPressed(KeyCode.RIGHT) || input.wasPressed(KeyCode.DOWN)) {
            choice.next();
        }
        if (input.wasPressed(KeyCode.ESCAPE) || input.wasPressed(KeyCode.ENTER)
                || input.wasPressed(KeyCode.SPACE)) {
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
        gc.setFill(PANEL);
        gc.fillRoundRect(centerX - PANEL_WIDTH / 2, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT, 28, 28);

        gc.setFont(font);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(GOLD);
        gc.fillText("CUSTOMISER", centerX, PANEL_Y + 55);

        drawCharacter(gc, centerX);
        drawArrows(gc, centerX, SPRITE_BOTTOM - 130);

        gc.setFill(GOLD);
        gc.fillText(choice.getName(), centerX, PANEL_Y + 460);

        gc.setFont(smallFont);
        gc.setFill(TEXT);
        gc.fillText("FLECHES POUR CHANGER   ENTREE OU ECHAP POUR REVENIR", centerX, App.HEIGHT - 30);
        gc.restore();
    }

    private void drawCharacter(GraphicsContext gc, double centerX) {
        gc.save();
        gc.setImageSmoothing(false);
        gc.translate(centerX, SPRITE_BOTTOM);
        gc.scale(SPRITE_SCALE, SPRITE_SCALE);
        sprite.render(gc, choice.getSheetPath(), pose, 0, new Hitbox(-20, -48, 40, 40));
        gc.restore();
    }

    private void drawArrows(GraphicsContext gc, double centerX, double y) {
        gc.setFill(GOLD);
        double left = centerX - ARROW_OFFSET;
        double right = centerX + ARROW_OFFSET;
        gc.fillPolygon(new double[] {left, left + ARROW_SIZE, left + ARROW_SIZE},
                new double[] {y, y - ARROW_SIZE, y + ARROW_SIZE}, 3);
        gc.fillPolygon(new double[] {right, right - ARROW_SIZE, right - ARROW_SIZE},
                new double[] {y, y - ARROW_SIZE, y + ARROW_SIZE}, 3);
    }
}
