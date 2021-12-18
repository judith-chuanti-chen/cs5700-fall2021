package common;

public class Config {
  // Port numbers used by unreliable network layers.
  public static final int SENDER_LISTEN_PORT = 8080;
  public static final int RECEIVER_LISTEN_PORT = 8081;

  // Parameters for unreliable network.
  public static final double BIT_ERROR_PROB = 0.1;
  public static final double MSG_LOST_PROB = 0.1;

  // Parameters for transport protocols.
  // TODO: Change TIMEOUT_MESC = 150
  public static final int TIMEOUT_MSEC = 150;
  public static final int WINDOW_SIZE = 20;

  // Packet size for network layer.
  public static final int MAX_SEGMENT_SIZE = 512;

  // Packet size for transport layer.
  public static final int MAX_MESSAGE_SIZE = 500;

  // Message types used in transport layer.
  public static final short MSG_TYPE_DATA = 1;
  public static final short MSG_TYPE_ACK = 2;

  public static final int TYPE_LENGTH = 2;
  public static final int SEQ_NUM_LENGTH = 2;
  public static final int CHECKSUM_LENGTH = 2;

  public static final int MAX_SEQ_NUM = 65535;
}
