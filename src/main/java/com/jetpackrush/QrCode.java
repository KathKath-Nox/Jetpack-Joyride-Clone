package com.jetpackrush;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.Map;

/** Fabrique la grille d'un QR code, que le menu dessine ensuite case par case. */
final class QrCode {

    private QrCode() {
    }

    /**
     * Encode un texte en QR code.
     *
     * @param text le texte à encoder
     * @return la grille carrée, {@code true} pour une case noire (sans marge autour)
     * @throws IllegalArgumentException si le texte est trop long pour un QR code
     */
    static boolean[][] encode(String text) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0,
                Map.of(EncodeHintType.MARGIN, 0));
            boolean[][] cells = new boolean[matrix.getHeight()][matrix.getWidth()];
            for (int y = 0; y < matrix.getHeight(); y++) {
                for (int x = 0; x < matrix.getWidth(); x++) {
                    cells[y][x] = matrix.get(x, y);
                }
            }
            return cells;
        } catch (WriterException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
