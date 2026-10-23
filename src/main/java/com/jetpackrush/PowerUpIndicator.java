package com.jetpackrush;

import java.io.InputStream;
import java.util.Locale;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Indicateur des power-ups affiché en haut à gauche : le minuteur du véhicule (par exemple 07.3S) et
 * le libellé BOUCLIER avec sa bulle, dessinés avec la police {@code jetpack_font.ttf}. Rien n'est
 * dessiné si aucun power-up n'est actif.
 */
public class PowerUpIndicator {
    private static final double NUMBER_SIZE = 44;
    private static final double UNIT_SIZE = 26;
    private static final double LABEL_SIZE = 28;
    private static final double LINE_HEIGHT = 46;

    private Font numberFont;
    private Font unitFont;
    private Font labelFont;

    /**
     * Dessine les indicateurs des power-ups actifs du joueur. Les deux lignes s'empilent si le joueur
     * a à la fois le véhicule et le bouclier.
     *
     * @param gc     contexte graphique du canvas
     * @param player le joueur dont on affiche les power-ups
     * @param x      position horizontale du texte
     * @param y      position verticale de la première ligne
     */
    public void render(GraphicsContext gc, Player player, double x, double y) {
        boolean riding = player.getState() instanceof Riding;
        if (!riding && !player.hasShield()) {
            return;
        }
        loadFonts();
        double line = y;
        if (riding) {
            drawTimer(gc, (Riding) player.getState(), x, line);
            line += LINE_HEIGHT;
        }
        if (player.hasShield()) {
            drawShield(gc, x, line);
        }
    }

    private void drawTimer(GraphicsContext gc, Riding riding, double x, double y) {
        double timeLeft = Math.max(0, riding.getTimeLeft());
        String number = String.format(Locale.ROOT, "%04.1f", timeLeft);
        Text measure = new Text(number);
        measure.setFont(numberFont);
        double width = measure.getLayoutBounds().getWidth();
        draw(gc, number, numberFont, x, y);
        draw(gc, "S", unitFont, x + width + 4, y);
    }

    private void drawShield(GraphicsContext gc, double x, double y) {
        Image bubble = AssetImages.get("/assets/shield_bubble.png");
        double textX = x;
        if (bubble != null) {
            gc.save();
            gc.setImageSmoothing(false);
            gc.drawImage(bubble, x, y - 30, 36, 36);
            gc.restore();
            textX = x + 44;
        }
        draw(gc, "BOUCLIER", labelFont, textX, y);
    }

    private void draw(GraphicsContext gc, String text, Font font, double x, double y) {
        gc.setFont(font);
        gc.setLineWidth(6);
        gc.setStroke(Color.rgb(25, 25, 25));
        gc.strokeText(text, x, y);
        gc.setFill(Color.rgb(240, 240, 240));
        gc.fillText(text, x, y);
    }

    private void loadFonts() {
        if (numberFont != null) {
            return;
        }
        InputStream in = PowerUpIndicator.class.getResourceAsStream("/assets/jetpack_font.ttf");
        Font loaded = in == null ? null : Font.loadFont(in, NUMBER_SIZE);
        numberFont = loaded != null ? loaded : Font.font(NUMBER_SIZE);
        unitFont = Font.font(numberFont.getFamily(), UNIT_SIZE);
        labelFont = Font.font(numberFont.getFamily(), LABEL_SIZE);
    }
}
