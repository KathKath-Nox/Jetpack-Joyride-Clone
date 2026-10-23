package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Affichage de la distance et des pièces en haut à gauche. Observateur du {@link ScoreManager} :
 * il garde les dernières valeurs reçues et les dessine.
 */
public class ScoreHud implements ScoreListener {

    private static final double FONT_SIZE = 40;
    private static final double ICON_SIZE = 32;
    private static final double BEST_FONT_SIZE = 28;
    private static final double BEST_MARGIN = 20;
    private static final double RUSH_FONT_SIZE = 44;

    private int distance;
    private int coins;
    private int best;
    private Font font;
    private Font bestFont;
    private Font rushFont;
    private boolean rush;

    @Override
    public void onScoreChanged(int distance, int coins) {
        this.distance = distance;
        this.coins = coins;
    }

    /**
     * Définit le meilleur score à afficher en haut à droite.
     *
     * @param best meilleure distance des parties précédentes, en mètres (0 : rien n'est affiché)
     */
    void setBest(int best) {
        this.best = best;
    }

    /**
     * Active ou non l'affichage « RUSH » en haut au centre (mode Rush).
     *
     * @param rush {@code true} pour afficher l'étiquette
     */
    void setRush(boolean rush) {
        this.rush = rush;
    }

    /**
     * Dessine le score.
     *
     * @param gc le contexte graphique du canvas
     * @param x  bord gauche
     * @param y  ligne de base du premier texte
     */
    public void render(GraphicsContext gc, double x, double y) {
        if (font == null) {
            font = Assets.font(FONT_SIZE);
        }
        draw(gc, distance + " M", x, y);
        Image sheet = AssetImages.get("/assets/coins_sprite_sheet.png");
        double textX = x;
        if (sheet != null) {
            gc.save();
            gc.setImageSmoothing(false);
            gc.drawImage(sheet, 0, 0, sheet.getWidth() / 6, sheet.getHeight(), x, y + 14, ICON_SIZE, ICON_SIZE);
            gc.restore();
            textX = x + ICON_SIZE + 8;
        }
        draw(gc, String.valueOf(coins), textX, y + 46);
        if (best > 0) {
            drawBest(gc, y);
        }
        if (rush) {
            drawRush(gc, y);
        }
    }

    private void drawRush(GraphicsContext gc, double y) {
        if (rushFont == null) {
            rushFont = Assets.font(RUSH_FONT_SIZE);
        }
        gc.save();
        gc.setFont(rushFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setLineWidth(6);
        gc.setStroke(Color.rgb(25, 25, 25));
        gc.strokeText("RUSH", App.WIDTH / 2.0, y);
        gc.setFill(Color.GOLD);
        gc.fillText("RUSH", App.WIDTH / 2.0, y);
        gc.restore();
    }

    private void drawBest(GraphicsContext gc, double y) {
        if (bestFont == null) {
            bestFont = Assets.font(BEST_FONT_SIZE);
        }
        String text = "MEILLEUR : " + best + " M";
        gc.save();
        gc.setFont(bestFont);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setLineWidth(5);
        gc.setStroke(Color.rgb(25, 25, 25));
        gc.strokeText(text, App.WIDTH - BEST_MARGIN, y);
        gc.setFill(Color.rgb(240, 240, 240));
        gc.fillText(text, App.WIDTH - BEST_MARGIN, y);
        gc.restore();
    }

    private void draw(GraphicsContext gc, String text, double x, double y) {
        gc.save();
        gc.setFont(font);
        gc.setLineWidth(6);
        gc.setStroke(Color.rgb(25, 25, 25));
        gc.strokeText(text, x, y);
        gc.setFill(Color.rgb(240, 240, 240));
        gc.fillText(text, x, y);
        gc.restore();
    }
}