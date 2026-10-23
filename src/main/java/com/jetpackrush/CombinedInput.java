package com.jetpackrush;

/**
 * Réunit deux {@link InputSource} (par exemple clavier et téléphone) : une commande est active
 * si l'une des deux sources la demande.
 */
public class CombinedInput implements InputSource {

    private final InputSource first;
    private final InputSource second;

    /**
     * @param first  première source
     * @param second seconde source
     */
    public CombinedInput(InputSource first, InputSource second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean isThrustPressed() {
        return first.isThrustPressed() || second.isThrustPressed();
    }

    @Override
    public boolean isPausePressed() {
        // Les deux sont lues : une source peut effacer sa demande en la lisant
        boolean a = first.isPausePressed();
        boolean b = second.isPausePressed();
        return a || b;
    }
}
