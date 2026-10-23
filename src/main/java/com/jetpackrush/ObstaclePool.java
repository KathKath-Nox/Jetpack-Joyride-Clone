package com.jetpackrush;
//réutiliser les obstacles au lieu d'en créer de nouveaux à chaque fois
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

/** Réserve d'obstacles réutilisables (pattern Object Pool) : évite de recréer des objets à chaque apparition. */
public class ObstaclePool {
    private final ObstacleFactory factory;
    private final Map<ObstacleType, Deque<Obstacle>> free = new EnumMap<>(ObstacleType.class);

    /**
     * Crée un pool vide.
     *
     * @param factory factory utilisée quand aucun obstacle recyclé n'est disponible
     */
    public ObstaclePool(ObstacleFactory factory) {
        this.factory = factory;
    }
    //Le constructeur reçoit une ObstacleFactory et la stocke dans l'attribut factory.
    /**
     * Donne un obstacle du type demandé, recyclé s'il en reste, créé sinon.
     *
     * @param type type d'obstacle voulu
     * @param x position horizontale
     * @param y position verticale
     * @return un obstacle prêt à l'emploi
     */
    public Obstacle acquire(ObstacleType type, double x, double y) {
        Deque<Obstacle> available = free.get(type);//récupère la collection d'obstacles disponibles correspondant au type demandé.
        if (available != null && !available.isEmpty()) {
            Obstacle obstacle = available.pop();//si la collection n'est pas vide, on récupère un obstacle de la collection.
            obstacle.reset(x, y);//on réinitialise la position de l'obstacle avec les coordonnées x et y fournies.
            return obstacle;
        }
        return factory.create(type, x, y);
    }//récupérer un obstacle pour l'utiliser dans le jeu.

    /**
     * Rend un obstacle au pool pour qu'il soit réutilisé plus tard.
     *
     * @param obstacle obstacle devenu inutile
     */
    public void release(Obstacle obstacle) {
        Deque<Obstacle> available = free.computeIfAbsent(obstacle.getType(), type -> new ArrayDeque<>());
        if (!available.contains(obstacle)) {
            available.push(obstacle);
        }
    }//emet un obstacle dans la réserve lorsqu'il n'est plus utilisé.

    /**
     * @param type type d'obstacle
     * @return le nombre d'obstacles de ce type disponibles dans le pool
     */
    public int freeCount(ObstacleType type) {
        Deque<Obstacle> available = free.get(type);
        return available == null ? 0 : available.size();
    }
}