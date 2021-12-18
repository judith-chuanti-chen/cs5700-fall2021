package transportlayer;

import networklayer.NetworkLayer;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static common.Config.*;

public class GoBackN extends TransportLayer {
  private final Semaphore sem;
  private Deque<Packet> buffer;
  private final Object bufferLock;
  private final Object timerLock;
  private volatile int base;
  private volatile int nextSeqNum;
  private int expectedSeqNum;
  private volatile byte[] payload;
  private ScheduledFuture<?> timer;
  private ScheduledExecutorService scheduler;
  private Thread receiveAck;

  public GoBackN(NetworkLayer networkLayer) {
    super(networkLayer);
    sem = new Semaphore(WINDOW_SIZE);  // Guard to send N packets at a time.
    buffer = new ArrayDeque<>();
    bufferLock = new Object();
    timerLock = new Object();
    scheduler = new ScheduledThreadPoolExecutor(1);
    receiveAck = getReceiveAckThread();

  }

  @Override
  public void send(byte[] data) throws IOException {
    Thread receiveAck = getReceiveAckThread();
    if (!receiveAck.isAlive()){
      receiveAck.start();
    }
    try {
      sem.acquire();
      if (nextSeqNum < (base + WINDOW_SIZE) % MAX_SEQ_NUM) {
        Packet p = Packet.createPacket(MSG_TYPE_DATA, (short) nextSeqNum, data);
        synchronized (bufferLock) {
          buffer.offer(p);
        }
        networkLayer.send(p.toByteArray());
        if (base == nextSeqNum) {
          startTimer();
        }
        nextSeqNum = (nextSeqNum + 1) % MAX_SEQ_NUM;
      } else {
        System.out.println("window full, refuse data, available permits: " + sem.availablePermits());
      }
    } catch (InterruptedException | IllegalMsgTypeException | IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public byte[] recv() throws IOException {
    Thread receiverThread = new Thread(() -> {
      try {
        while(true) {
          byte[] data = networkLayer.recv();
          Packet p = Packet.parsePacket(data);
          Packet ackPacket;
          if (!p.isCorrupted() && p.getType() == MSG_TYPE_DATA && p.getSeqNum() == expectedSeqNum) {
            payload = p.getPayload();
            ackPacket = Packet.createAckPacket(MSG_TYPE_ACK, (short) expectedSeqNum);
            expectedSeqNum = (expectedSeqNum + 1) % MAX_SEQ_NUM ;
            networkLayer.send(ackPacket.toByteArray());
            break;
          } else {
            short ackNum = (short) ((expectedSeqNum - 1 + MAX_SEQ_NUM) % MAX_SEQ_NUM);
            ackPacket = Packet.createAckPacket(MSG_TYPE_ACK, ackNum);
            if (p.isCorrupted()) {System.out.println(String.format("packet corrupted: seqNum=%s, sentAck=%s",p.getSeqNum() & 0XFFFF, ackNum & 0XFFFF));}
            else { System.out.println(String.format("unexpected seqNum: seqNum=%s, sentAck=%s", p.getSeqNum() & 0XFFFF, ackNum & 0XFFFF)); }
            networkLayer.send(ackPacket.toByteArray());
          }
        }
      } catch (IOException | IllegalMsgTypeException e) {
        e.printStackTrace();
      }
    });
    receiverThread.start();
    try {
      receiverThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return payload;
  }

  private void startTimer() {
    synchronized (timerLock) {
      if (timer != null) { timer.cancel(true); }
      timer = scheduler.scheduleAtFixedRate(() -> {
                synchronized(bufferLock) {
                  for(Packet p : buffer) {
                    try {
                      networkLayer.send(p.toByteArray());
                    } catch (IOException | IllegalMsgTypeException e) {
                      e.printStackTrace();
                    }
                  }
                }
              },
              TIMEOUT_MSEC,
              TIMEOUT_MSEC,
              TimeUnit.MILLISECONDS);
    }
  }

  private Thread getReceiveAckThread() {
    if (receiveAck != null) {
      return receiveAck;
    }
    return new Thread(() -> {
      while (true) {
        try {
          byte[] data = networkLayer.recv();
          Packet ackPacket = Packet.parsePacket(data);
          if (ackPacket.getType() == MSG_TYPE_ACK && !ackPacket.isCorrupted()) {
            int ackNum = ackPacket.getSeqNum() & 0XFFFF;
            System.out.println("ack received ok: " + ackNum);
            // 2 special scenarios:
            // 1) Before sending ACK, ackNum > MAX_SEQ_NUM, but after modulo MAX_SEQ_NUM, now ackNum < base: numAcked <= WINDOW_SIZE
            // 2) Before sending ack, ackNum < MAX_SEQ_NUM, and ackNum < base: numAcked > WINDOW_SIZE, do nothing
            int numAcked = (ackNum < base ? MAX_SEQ_NUM : 0) + ackNum - base + 1;
            if (buffer.size() > 0 && numAcked <= WINDOW_SIZE) {
              synchronized (bufferLock) {
                while(buffer.size() > 0 && numAcked > 0) {
                  buffer.poll();
                  sem.release();
                  numAcked--;
                }
              }
              base = (ackNum + 1) % MAX_SEQ_NUM;
              // TODO: delete debug
              List<Short> seqNums = buffer.stream().map(Packet::getSeqNum).collect(Collectors.toList());
              String debug = String.format("updated buffer: ackNum=%s, base=%s, buffer=%s, permits available=%s",
                      ackNum, base, seqNums.toString(), sem.availablePermits());
              System.out.println(debug);
            }
            if (base == nextSeqNum) {
              synchronized (timerLock) {
                timer.cancel(true);
              }
            } else {
              startTimer();
            }
          } else {
            System.out.println("ack corrupted: " + (ackPacket.getSeqNum() & 0XFFFF));
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public void close() throws IOException {
    super.close();
    receiveAck.interrupt();
    System.out.println("receivedAck Thread interrupted");
  }
}
