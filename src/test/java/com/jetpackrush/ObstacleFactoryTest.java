package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ObstacleFactoryTest {
    private final ObstacleFactory factory = new ObstacleFactory(new GameSpeed(100));

    @Test
    void createsEveryTypeWithTheRightType() {
        for (ObstacleType type : ObstacleType.values()) {
            assertEquals(type, factory.create(type, 10, 20).getType());
        }
    }

    @Test
    void placesScrollingObstaclesAtTheGivenPosition() {
        for (ObstacleType type : ObstacleType.values()) {
            if (type == ObstacleType.LASER) {
                continue;
            }
            Obstacle obstacle = factory.create(type, 10, 20);
            assertEquals(10, obstacle.getX(), 1e-9);
            assertEquals(20, obstacle.getY(), 1e-9);
        }
    }

    @Test
    void lasersStartAtTheLeftEdgeAtTheGivenHeight() {
        Obstacle laser = factory.create(ObstacleType.LASER, 999, 20);
        assertEquals(0, laser.getX(), 1e-9);
        assertEquals(20, laser.getY(), 1e-9);
    }
}
