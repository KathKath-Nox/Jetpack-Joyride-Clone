package com.jetpackrush;

/** Ce qui peut arriver pendant une partie et mérite un son, un effet... (voir {@link GameEvents}). */
public enum GameEvent {
    /** Le joueur ramasse une pièce. */
    COIN_COLLECTED,
    /** Un choc est absorbé par le bouclier ou détruit le véhicule : le joueur survit. */
    PROTECTION_LOST,
    /** Un obstacle tue le joueur. */
    PLAYER_SHOCKED,
    /** Le jetpack se met à pousser. */
    THRUST_STARTED,
    /** Le jetpack s'arrête de pousser (touche relâchée, véhicule, mort). */
    THRUST_STOPPED
}
