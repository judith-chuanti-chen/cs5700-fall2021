package common;

import transportlayer.Packet;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static common.Config.*;

public class Util {
  public static void log(String msg) {
    System.out.println(msg);
  }

  public static void log(byte[] msg) {
    StringBuilder sb = new StringBuilder();
    for (byte b : msg) {
      sb.append(String.format("%02X", b));
    }
    log(sb.toString());
  }

  // Introduce random bit error to data.
  public static byte[] randomBitError(byte[] data) {
    int i = ThreadLocalRandom.current().nextInt(data.length);
    data[i] = (byte) ~data[i];
    return data;
  }

  public static DatagramPacket createDatagram(Packet p) {
    int payloadLength = p.getPayload().length;
    byte[] data = ByteBuffer.allocate(TYPE_LENGTH + SEQ_NUM_LENGTH + CHECKSUM_LENGTH + payloadLength)
            .put(ByteBuffer.allocate(TYPE_LENGTH).putShort(p.getType()))
            .put(ByteBuffer.allocate(SEQ_NUM_LENGTH).putShort(p.getSeqNum()))
            .put(ByteBuffer.allocate(CHECKSUM_LENGTH).putShort(p.getChecksum()))
            .array();
    return new DatagramPacket(data, data.length);
  }


  public static void main(String... args) {
    if (args.length > 0) {
      for (String arg : args) {
        log(arg + " => " + new String(randomBitError(arg.getBytes())));
      }
      return;
    }
    byte[] data = new byte[3];
    Arrays.fill(data, (byte) 0xFF);
    log(data);
    randomBitError(data);
    log(data);

    Arrays.fill(data, (byte) 0x45);
    log(data);
    randomBitError(data);
    log(data);
  }
}
