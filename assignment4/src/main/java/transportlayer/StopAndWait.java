package transportlayer;

import networklayer.NetworkLayer;

import java.io.IOException;
import java.util.concurrent.*;

import static common.Config.*;

// TODO.
public class StopAndWait extends TransportLayer {
  private Semaphore sem;
  private ScheduledFuture<?> timer;
  private ScheduledExecutorService scheduler;
  private short seqNum;
  private short expectedSeqNum;
  private volatile byte[] payload;
  public StopAndWait(NetworkLayer networkLayer) {
    super(networkLayer);
    sem = new Semaphore(1);  // Guard to send 1 pkt at a time.
    scheduler = Executors.newScheduledThreadPool(1);
  }

  @Override
  public void send(byte[] data) throws IOException {
    try {
      System.out.println("locked");
      sem.acquire();
      Packet p = Packet.createPacket(MSG_TYPE_DATA, seqNum, data);
      networkLayer.send(p.toByteArray().clone());
      timer = scheduler.scheduleAtFixedRate(new RetransmissionTask(p.toByteArray().clone()),
              TIMEOUT_MSEC,
              TIMEOUT_MSEC,
              TimeUnit.MILLISECONDS);
    } catch (InterruptedException | IllegalMsgTypeException e) {
      e.printStackTrace();
    }
    // Start thread to read ACK.
    new Thread(() -> {
      try {
        Packet ackPacket;
        do {
          byte[] ackData = networkLayer.recv();
          ackPacket = Packet.parsePacket(ackData);
          if (ackPacket.getType() == MSG_TYPE_ACK && ackPacket.getSeqNum() == seqNum && !ackPacket.isCorrupted()){
            System.out.println("ACK not corrupted: " + ackPacket.getSeqNum());
            timer.cancel(true);
            seqNum = (short) (1 - seqNum);
            sem.release();
            System.out.println("released");
          } else if (ackPacket.getType() == MSG_TYPE_ACK) {
            System.out.println("ACK corrupted or wrong seqNum, ignore: " + ackPacket.isCorrupted() +  ", " + (ackPacket.getSeqNum() & 0xFFFF));
          }
        }while(ackPacket == null || ackPacket.getSeqNum() != seqNum || ackPacket.isCorrupted());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }

  @Override
  public byte[] recv() throws IOException {
    // Start thread to read Data.
    ReceiverTask task = new ReceiverTask();
    Thread thread = new Thread(task);
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return payload;
  }

  @Override
  public void close() throws IOException {
    try {
      sem.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    super.close();
  }

  private class RetransmissionTask implements Runnable {
    private byte[] data;

    public RetransmissionTask(byte[] data) {
      this.data = data;
    }

    @Override
    public void run() {
      try {
        networkLayer.send(data.clone());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  private class ReceiverTask implements Runnable {
    public ReceiverTask() {
    }

    @Override
    public void run() {
      try {
        payload = null;
        while(payload == null) {
          byte[] data = networkLayer.recv();
          Packet packet = Packet.parsePacket(data);
          Packet ackPacket;
//          System.out.println("received payload in task.run(): " + "seqNum = " + packet.getSeqNum());
          System.out.println("received payload in task.run(): " + new String(packet.getPayload() ) + ", seqNum = " + packet.getSeqNum());
          if (!packet.isCorrupted() && packet.getType() == MSG_TYPE_DATA && packet.getSeqNum() == expectedSeqNum) {
            System.out.println("not corrupted, has right seqNum");
            payload = packet.getPayload();
            ackPacket = Packet.createAckPacket(MSG_TYPE_ACK, expectedSeqNum);
            networkLayer.send(ackPacket.toByteArray());
            expectedSeqNum = (short) (1 - expectedSeqNum);
          } else if (packet.getSeqNum() != expectedSeqNum || packet.isCorrupted()){
            if (packet.getSeqNum() != expectedSeqNum) {
              System.out.println("actual seqNum=" + packet.getSeqNum() + ", expected seqNum=" + expectedSeqNum);
            } else {
              System.out.println("packet corrupted");
            }
            ackPacket = Packet.createAckPacket(MSG_TYPE_ACK, (short) (1 - expectedSeqNum));
            networkLayer.send(ackPacket.toByteArray());
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
