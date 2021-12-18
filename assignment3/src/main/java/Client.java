import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class Client implements Runnable{
  private static final int SERVER_PORT = 8080;
  private static final int BUF_SIZE = 1024;
  // 0: waiting for game to start; 1: game started; -1: game ended
  private int state;
  private Message playerInfo;
  private Board board;
  private GameWindow window;
  // data to send
  private byte[] data;
  private DirectionChangeHandler dirChangeHandler;

  public Client(Message playerInfo){
    this.state = 0;
    this.playerInfo = playerInfo;
    this.dirChangeHandler = new DirectionChangeHandler();
  }

  public class DirectionChangeHandler extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
      Message.Builder m = new Message.Builder()
              .type(3)
              .gameId(playerInfo.getGameId())
              .nickName(playerInfo.getNickName());
      switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
          m.dir(0);
          break;
        case KeyEvent.VK_RIGHT:
          m.dir(1);
          break;
        case KeyEvent.VK_DOWN:
          m.dir(2);
          break;
        case KeyEvent.VK_LEFT:
          m.dir(3);
          break;
      }
      setData(MessageWriter.changeDirectionMsg(m.build()));
    }
  }

  public void run() {
    System.out.println("UDP client started.");
    try(DatagramSocket clientSocket = new DatagramSocket(this.playerInfo.getPort(), this.playerInfo.getIp())) {
      InetAddress serverIp = InetAddress.getLocalHost();
      this.setData(MessageWriter.createOrJoinGameMsg(playerInfo));
      clientSocket.send(new DatagramPacket(data, data.length, serverIp, SERVER_PORT));
      this.setData(null);
      while (this.state >= 0) {
        // Recv msg.
        byte[] buf = new byte[BUF_SIZE];
        DatagramPacket pktReceived = new DatagramPacket(buf, BUF_SIZE);
        clientSocket.receive(pktReceived);
        Message m = MessageReader.read(Arrays.copyOfRange(pktReceived.getData(), 0, pktReceived.getLength()));

        switch(m.getType()){
          case 4:
            // initialize game GUI;
            createGameGUI();
            board.setMessage("Waiting for opponent");
            board.setState(0);
            board.repaint();
            System.out.println("Waiting for opponent");
            break;
          case 5:
            if (board == null && window == null){
              createGameGUI();
            }
            board.setMessage("Game is about to start");
            board.setState(0);
            board.repaint();
            System.out.println("Game is about to start");
            break;
          case 6:
            String message = String.format("Game over! Result: %s", m.getResult())
                    + (m.getWinner() != null ? String.format(" Winner: %s", m.getWinner()) : "");
            board.setMessage(message);
            board.setState(0);
            board.repaint();
            System.out.println(message);
            break;
          case 7:
            board.setState(1);
            board.setApple(m.getApple());
            board.setSnake1(m.getSnake1());
            board.setSnake2(m.getSnake2());
            board.repaint();
            System.out.println(m);
            break;
          default:
            System.out.println(new String(pktReceived.getData()));
            this.state = -1;
            break;
        }
        if(data != null){
          clientSocket.send(new DatagramPacket(data, data.length, serverIp, SERVER_PORT));
          this.setData(null);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public void createGameGUI(){
    board = new Board();
    window = new GameWindow(board, dirChangeHandler);
    window.setVisible(true);
  }
}
