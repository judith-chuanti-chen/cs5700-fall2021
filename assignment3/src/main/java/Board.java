import javax.swing.*;

import java.awt.*;
import java.util.LinkedList;


public class Board extends JPanel {
    private static final Image GREEN_DOT = new ImageIcon("src/main/java/images/green_dot.png").getImage();
    private static final Image APPLE = new ImageIcon("src/main/java/images/apple.png").getImage();
    private static final Image RED_DOT = new ImageIcon("src/main/java/images/red_dot.png").getImage();
    private Position apple;
    private LinkedList<Position> snake1;
    private LinkedList<Position> snake2;
    // state=0: render message; state=1: render apple and snakes
    private int state;
    private String message;


    public Board() {
        this.state = 0;
        this.message = "Welcome to Snake Game";
        int size = Config.UNIT_SIZE * Config.BOARD_SIZE;
        setPreferredSize(new Dimension(size, size));
        setBorder(BorderFactory.createLineBorder(Color.BLUE));
        setBackground(Color.BLACK);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == 0) {
            renderMessage(g, message);
        } else {
            renderApple(g);
            renderSnake(g, snake1, GREEN_DOT);
            renderSnake(g, snake2, RED_DOT);
        }
    }

    private void renderApple(Graphics g) {
        render(g, APPLE, apple);
    }

    private void renderSnake(Graphics g, LinkedList<Position> snake, Image dot) {
        snake.forEach(p -> render(g, dot, p));
    }

    private void renderMessage(Graphics g, String message) {
        g.setColor(Color.PINK);
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.drawString(message, 30, 50);
    }

    private void render(Graphics g, Image image, Position p) {
        g.drawImage(image, p.getCol() * Config.UNIT_SIZE, p.getRow() * Config.UNIT_SIZE, this);
    }

    public void setApple(Position apple) {
        this.apple = apple;
    }

    public void setSnake1(LinkedList<Position> snake1) {
        this.snake1 = snake1;
    }

    public void setSnake2(LinkedList<Position> snake2) {
        this.snake2 = snake2;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
