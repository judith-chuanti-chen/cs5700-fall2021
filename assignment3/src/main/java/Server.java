import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Server implements Runnable{
  private static final int PORT = 8080;
  private static final int BUF_SIZE = 1024;
  private DatagramSocket serverSocket;
  private GameState gameState;
  private String gameId;
  private List<Message> players;
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> future;

  public Server() {
    this.gameState = GameState.get();
    this.players = new ArrayList<>();
    this.gameId  = "";
    this.scheduler = Executors.newSingleThreadScheduledExecutor();

    try {
      this.serverSocket = new DatagramSocket(PORT);
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    try {
      System.out.println("UDP server started.");
      while (!gameState.isGameOver()) {
        byte[] buf = new byte[BUF_SIZE];
        DatagramPacket pktReceived = new DatagramPacket(buf, BUF_SIZE);
        serverSocket.receive(pktReceived);
        InetAddress addr = pktReceived.getAddress();
        int port = pktReceived.getPort();
        Message m = MessageReader.read(Arrays.copyOfRange(pktReceived.getData(), 0, pktReceived.getLength()));
        byte[] data = "unidentified message".getBytes();
//        String msg = new String(pktReceived.getData());
        System.out.println(String.format("[%s:%d]: %s", addr, port, m));
        switch(m.getType()){
          case 1:
            if(players.size() > 0){
              data = "Sorry, a new game has already been created and is waiting to start.".getBytes();
            } else {
              players.add(m);
              this.gameId = m.getGameId();
              data =  MessageWriter.waitForSecondUserMsg();
          }
            serverSocket.send(new DatagramPacket(data, data.length, m.getIp(), m.getPort()));
            break;
          case 2:
            if (players.size() == 0) {
              data = "Sorry, please create a new game first".getBytes();
              serverSocket.send(new DatagramPacket(data, data.length, m.getIp(), m.getPort()));
            }
            else if (players.size() > 1) {
              data = "Sorry, there are already 2 players.".getBytes();
              serverSocket.send(new DatagramPacket(data, data.length, m.getIp(), m.getPort()));
            } else {
              // When there's only 1 player in the game
              if (!m.getGameId().equals(this.gameId)){
                data = "Sorry, the game ID doesn't match.".getBytes();
                serverSocket.send(new DatagramPacket(data, data.length, m.getIp(), m.getPort()));
              } else {
                // ready to start game
                players.add(m);
                data = MessageWriter.waitForGameStartMsg();
                for (Message playerInfo : players){
                  serverSocket.send(new DatagramPacket(data, data.length, playerInfo.getIp(), playerInfo.getPort()));
                }
                // Start game
                future = scheduler.scheduleAtFixedRate(this::runOnce, 1000, Config.GAME_SPEED_MS, MILLISECONDS);
              }
            }
            break;
          case 3:
            //change direction
            int idx = findPlayerIndex(m.getNickName());

            Direction d = convertValueToDirection(m.getDir());
            if (idx == 0) {
              gameState.updateDirection1(d);
            } else {
              gameState.updateDirection2(d);
            }
            break;
          default:
            serverSocket.send(new DatagramPacket(data, data.length, addr, port));
            break;
        }
        System.out.println("run() gameOver: " + gameState.isGameOver());
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      serverSocket.close();
    }
  }

  private void runOnce(){
    if (gameState.isGameOver()) {
      System.out.println("still running!");
      return;
    }
    gameState.moveSnakes();
    byte[] data = MessageWriter.gameStateMsg(0, gameState.getApplePosition(), gameState.getSnake1Position(),
            gameState.getSnake2Position());
    sendToBothPlayers(data);
    if (gameState.isGameOver()) {
      // Send game result after game is over
      System.out.println("game over, sending result players");
      String winner = gameState.getResult() == 0 ? null :
              (gameState.getResult() == 1 ? players.get(0).getNickName() : players.get(1).getNickName());
      byte result = (byte) (gameState.getResult() == 0 ? 0 : 1);
      data = MessageWriter.gameOverMsg(result, winner);
      sendToBothPlayers(data);
      boolean cancelled = future.cancel(false);
      System.out.println("runOnce: cancelled = " + cancelled);
      scheduler.shutdownNow();
      System.out.println("is shutdown: " + scheduler.isShutdown());
    }
  }

  private void sendToBothPlayers(byte[] data){
    for(Message playerInfo : players){
      try {
        serverSocket.send(new DatagramPacket(data, data.length, playerInfo.getIp(), playerInfo.getPort()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private int findPlayerIndex(String nickName){
    for(int i = 0; i < players.size(); i++){
      if (players.get(i).getNickName().equals(nickName)){
        return i;
      }
    }
    return 0;
  }

  private Direction convertValueToDirection(int dir){
    switch(dir){
      case 0:
        return Direction.UP;
      case 1:
        return Direction.RIGHT;
      case 2:
        return Direction.DOWN;
      case 3:
        return Direction.LEFT;
    }
    return Direction.UP;
  }
}
