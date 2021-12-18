import java.util.concurrent.ThreadLocalRandom;


public enum Direction {
  DOWN,
  UP,
  RIGHT,
  LEFT;

  public static Direction random() {
    // Since initial direction of the snake is facing up, direction cannot be down
    return Direction.values()[1 + ThreadLocalRandom.current().nextInt(3)];
  }
}
