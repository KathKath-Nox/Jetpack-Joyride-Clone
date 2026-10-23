package com.jetpackrush;
//Obstacle définit le comportement commun à tous les obstacles

/**
 * Classe de base de tous les dangers du jeu (zappers, missiles, lasers).
 * Elle gère la position, la hitbox, la sortie d'écran et le recyclage par {@link ObstaclePool}.
 */
public abstract class Obstacle extends Entity implements Collidable {
    private final GameSpeed speed;
    private boolean spent; // si l'obstacle a déjà été touché par le joueur

    /**
     * Crée un obstacle.
     *
     * @param x position horizontale de départ
     * @param y position verticale de départ
     * @param width largeur de la hitbox, en pixels
     * @param height hauteur de la hitbox, en pixels
     * @param speed vitesse de jeu partagée
     */
    protected Obstacle(double x, double y, double width, double height, GameSpeed speed) {
        super(x, y, width, height);
        this.speed = speed;
    }
    // x= position horizontale, y= position verticale, width= largeur de l'obstacle, height= hauteur de l'obstacle, speed= vitesse du jeu
    /**
     * @return le type de cet obstacle (utilisé par la factory et le pool)
     */
    public abstract ObstacleType getType();
    //type d'obstacle
    @Override
    public void update(double dt) {
        x -= speed.getSpeed() * dt;
    }
    // mise à jour de la position de l'obstacle en fonction du temps écoulé (dt) et de la vitesse du jeu

    @Override
    public void onCollision(Player player) {
        if (spent || player.isDead()) {
            return;
        }
        spent = true;
        player.hit();
    }
    // gestion de la collision avec le joueur, si l'obstacle a déjà été touché ou 
    // si le joueur est mort, on ne fait rien, sinon on marque l'obstacle comme touché et on inflige des dégâts au joueur

    /**
     * Teste la collision avec une hitbox.
     *
     * @param other hitbox à tester (en général celle du joueur)
     * @return true si l'obstacle touche cette hitbox
     */
    public boolean collidesWith(Hitbox other) {
        return getHitbox().intersects(other);
    }
    // vérifie si l'obstacle entre en collision avec une autre hitbox (par exemple celle du joueur)

    /**
     * @return true si l'obstacle est sorti de l'écran et peut être retiré
     */
    public boolean isOffScreen() {
        return x + width < 0;
    }
    // vérifie si l'obstacle est hors de l'écran

    /**
     * Remet l'obstacle à neuf à une nouvelle position pour le réutiliser (object pool).
     *
     * @param x nouvelle position horizontale
     * @param y nouvelle position verticale
     */
    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        this.spent = false;
    }
    // réinitialise la position de l'obstacle et marque l'obstacle comme non touché
    //pour pouvoir réutiliser l'obstacle dans le jeu

    /**
     * @return true si l'obstacle a terminé son action et peut être retiré
     */
    protected boolean isSpent() {
        return spent;
    }// vérifie si l'obstacle a déjà été touché par le joueur

    /**
     * @return la vitesse de jeu partagée
     */
    protected GameSpeed getSpeed() {
        return speed;
    }// retourne la vitesse du jeu
}
