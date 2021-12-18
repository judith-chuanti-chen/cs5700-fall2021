package applicationlayer;

import transportlayer.TransportLayer;
import transportlayer.TransportLayerFactory;

import static common.Config.SENDER_LISTEN_PORT;
import static common.Config.RECEIVER_LISTEN_PORT;
import static common.Util.log;

public class MsgSender {
  public static void main(String... args) throws Exception {
    if (args.length != 1) {
      log("Usage: MsgSender [transport-layer-name]");
      return;
    }
    String name = args[0];
    log("MsgSender start with transport layer " + name);
    TransportLayer t = TransportLayerFactory.create(name, SENDER_LISTEN_PORT, RECEIVER_LISTEN_PORT);
    for (int i = 0; i < 50; ++i) {
      String msg = "MSG:" + i;
      log(msg);
      t.send(msg.getBytes());
    }
    // wait for msgReceiver to receive all the packets
    Thread.sleep(10000);
    t.close();
    log("MsgSender complete");
  }
}
