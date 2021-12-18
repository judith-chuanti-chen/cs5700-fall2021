package applicationlayer;

import transportlayer.TransportLayer;
import transportlayer.TransportLayerFactory;

import static common.Config.MAX_MESSAGE_SIZE;
import static common.Config.SENDER_LISTEN_PORT;
import static common.Config.RECEIVER_LISTEN_PORT;
import static common.Util.log;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class FileSender {
  public static void main(String... args) throws Exception {
    if (args.length != 2) {
      log("Usage: FileSender [transport-layer-name] [file-name]");
      return;
    }
    String name = args[0];
    String file = args[1];
    log("FileSender start");
    TransportLayer t = TransportLayerFactory.create(name, SENDER_LISTEN_PORT, RECEIVER_LISTEN_PORT);
    InputStream inputStream = new FileInputStream(file);
    byte[] buf = new byte[MAX_MESSAGE_SIZE];
    int len;
    while ((len = inputStream.read(buf)) != -1) {
      log("MSG of length " + len);
      t.send(Arrays.copyOf(buf, len));
    }
    // wait for fileReceiver to receive all the packets
    Thread.sleep(10000);
    t.close();
    log("FileSender complete");
  }
}
