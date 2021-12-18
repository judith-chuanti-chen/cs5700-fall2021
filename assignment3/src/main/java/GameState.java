import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameState {
    private static GameState INSTANCE;

    private Position apple;
    private LinkedList<Position> snake1;
    private LinkedList<Position> snake2;
    private Direction dir1;
    private Direction dir2;
    private boolean gameOver;
    // 0: draw; 1: snake1 won; 2: snake2 won;
    private int result;

    private GameState() {
        snake1 = initSnake();
        snake2 = initSnake();
        generateApple();
        dir1 = Direction.random();
        dir2 = Direction.random();
        gameOver = false;
    }

    public static synchronized GameState get() {
        if (INSTANCE == null) {
            INSTANCE = new GameState();
        }
        return INSTANCE;
    }

    public Position getApplePosition() {
        return apple;
    }

    public LinkedList<Position> getSnake1Position() {
        return snake1;
    }

    public LinkedList<Position> getSnake2Position() {
        return snake2;
    }

    public int getResult() {
        return result;
    }

    public LinkedList<Position> initSnake() {
        Position head = Position.random(Config.BOARD_SIZE);
        return Stream.of(head,
                Position.copy(head).move(Direction.DOWN),
                Position.copy(head).move(Direction.DOWN).move(Direction.DOWN))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void updateDirection1(Direction newDir) {
        Position next = Position.copy(snake1.getFirst()).move(newDir);
        if (next.equals(snake1.get(1))) {
            return;
        }
        dir1 = newDir;
    }

    public void updateDirection2(Direction newDir) {
        Position next = Position.copy(snake2.getFirst()).move(newDir);
        if (next.equals(snake2.get(1))) {
            return;
        }
        dir2 = newDir;
    }

    public void moveSnakes() {
        if (gameOver) {
            System.out.println("GameState::moveSnakes gameOver=true");
            return;
        }
        Position nextHead1 = Position.copy(snake1.getFirst()).move(dir1);
        Position nextHead2 = Position.copy(snake2.getFirst()).move(dir2);
        if (isInSnakeButNotHeadOrTail(nextHead1, snake1) || isOnBorder(nextHead1)){
            gameOver = true;
            result = 2;
            System.out.println("GameState::moveSnakes gameOver=true green snake touches border");
            return;
        }
        if (isInSnakeButNotHeadOrTail(nextHead2, snake2) || isOnBorder(nextHead2)) {
            gameOver = true;
            result = 1;
            System.out.println("GameState::moveSnakes gameOver=true red snake touches border");
            return;
        }
        // Draw
        if (nextHead1.equals(nextHead2)){
            gameOver = true;
            result = 0;
            System.out.println("GameState::moveSnakes gameOver=true draw");
            return;
        }
        snake1.addFirst(nextHead1);
        snake2.addFirst(nextHead2);
        if (nextHead1.equals(apple)) {
            generateApple();
            snake2.removeLast();
            return;
        }
        if (nextHead2.equals(apple)) {
            generateApple();
            snake1.removeLast();
            return;
        }
        snake1.removeLast();
        snake2.removeLast();
    }

    private void generateApple() {
        do {
            apple = Position.random(Config.BOARD_SIZE);
        } while (isInSnake(apple, snake1) || isInSnake(apple, snake2));
    }

    private boolean isInSnake(Position o, LinkedList<Position> snake) {
        return snake.stream().anyMatch(p -> p.equals(o));
    }

    private boolean isInSnakeButNotHeadOrTail(Position o, LinkedList<Position> snake) {
        for (Position p : snake) {
            if (p.equals(snake.getFirst()) || p.equals(snake.getLast())) {
                continue;
            }
            if (p.equals(o)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnBorder(Position o){
        if (o.getCol() == 0 || o.getRow() == 0 || o.getCol() == Config.BOARD_SIZE || o.getRow() == Config.BOARD_SIZE){
            return true;
        }
        return false;
    }
}

