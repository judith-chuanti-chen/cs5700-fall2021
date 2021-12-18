import javax.swing.*;

public class GameWindow extends JFrame {
    private final Board board;
    private final Client.DirectionChangeHandler directionChangeHandler;

    public GameWindow(Board board, Client.DirectionChangeHandler directionChangeHandler){
        setTitle("Snake Game");
        this.board = board;
        this.directionChangeHandler = directionChangeHandler;
        add(this.board);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Size the window so that all its contents are at or above their
        // preferred sizes.
        pack();
        // TODO: Modify DirectionChangeHandler
        addKeyListener(this.directionChangeHandler);
    }

}
