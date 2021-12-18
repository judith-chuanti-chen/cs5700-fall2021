import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MessageReader {
    private static final String CREATE = "create";
    private static final String JOIN = "join";

    public static Message parseCommand(String[] args){
        if (args.length < 5) return null;
        final String commandType = args[0];
        int type;
        Message message = null;
        switch(commandType) {
            case CREATE:
                type = 1 ;
                break;
            case JOIN:
                type = 2;
                break;
            default:
                return null;
        }
        String gameId = args[1], nickName = args[2];
        try {
            InetAddress ip = InetAddress.getByName(args[3]);
            int port = Integer.parseInt(args[4]);
            message = new Message.Builder()
                    .type(type)
                    .gameId(gameId)
                    .nickName(nickName)
                    .ip(ip)
                    .port(port)
                    .build();
        } catch(UnknownHostException | NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Illegal input for IP or Port number");
        }
        return message;
    }
    public static Message read(byte[] bytes) throws Exception {
        if (bytes == null || bytes.length == 0) return null;
        byte type = bytes[0];
        Message msg = null;
        switch(type){
            case 1:
            case 2:
                msg = createOrJoinGame(bytes);
                break;
            case 3:
                msg = changeDirection(bytes);
                break;
            case 4:
                msg = new Message.Builder().type(4).build();
                break;
            case 5:
                msg = new Message.Builder().type(5).build();
                break;
            case 6:
                msg = gameOver(bytes);
                break;
            case 7:
                msg = gameState(bytes);
                break;
            default:
                // unidentified message
//                msg = new Message.Builder().type(0).build();
                break;
        }
        return msg;
    }
    public static Message.Builder parseBasicInfo(byte[] bytes){
        int type = bytes[0] & 0xff, idLength = bytes[1] & 0xff, nameLength = bytes[2] & 0xff;
        int i = 3;
        String gameId = new String(Arrays.copyOfRange(bytes, i, i+ idLength));
        i += idLength;
        String nickName = new String(Arrays.copyOfRange(bytes, i, i + nameLength));
        return new Message.Builder()
                    .type(type)
                    .gameId(gameId)
                    .nickName(nickName);
    }

    public static Message createOrJoinGame(byte[] bytes) throws UnknownHostException {
        if (bytes.length < 9) return null;
        Message.Builder msgBuilder = parseBasicInfo(bytes);
        int i = 3 + msgBuilder.getGameId().length() + msgBuilder.getNickName().length();
        InetAddress ip = InetAddress.getByAddress(Arrays.copyOfRange(bytes, i, i + 4));
        i += 4;
        int port = new BigInteger(Arrays.copyOfRange(bytes, i, i + 2)).intValue();
        return msgBuilder
                .ip(ip)
                .port(port)
                .build();
    }

    public static Message changeDirection(byte[] bytes){
        if (bytes.length < 6) return null;
        Message.Builder msgBuilder = parseBasicInfo(bytes);
        int i = 3 + msgBuilder.getGameId().length() + msgBuilder.getNickName().length();
        int dir = bytes[i] & 0xff;
        return msgBuilder
                .dir(dir)
                .build();
    }

    public static Message gameOver(byte[] bytes) {
        if (bytes.length < 2) return null;
        int type = bytes[0] & 0xff;
        int result = bytes[1] & 0xff;
        Message.Builder msgBuilder = new Message.Builder();
        msgBuilder.type(type).result(result);
        if (result == 1) {
            int nameLength = bytes[2] & 0xff;
            int i = 3;
            String winnerName = new String(Arrays.copyOfRange(bytes, i, i + nameLength));
            msgBuilder.winner(winnerName);
        }
        return msgBuilder.build();
    }

    public static Message gameState(byte[] bytes) {
        if (bytes.length < 260) return null;
        int type = bytes[0] & 0xff;
        int sequence = bytes[1] & 0xff;
        Position apple = new Position(bytes[2] & 0xff, bytes[3] & 0xff, Config.BOARD_SIZE);
        int start = 4;
        LinkedList<Position> snake1 = new LinkedList<>();
        LinkedList<Position> snake2 = new LinkedList<>();

        for(int i = start; i < 128 + start; i++){
            if ((bytes[i] & 0xff) != 0) {
                List<Integer> ones = readBitmap(bytes[i]);
                int row = (i - start) / 4, col = ((i - start) % 4) * 8;
                for (Integer onePosition : ones){
                    Position p = new Position(row, col + onePosition, Config.BOARD_SIZE);
                    snake1.add(p);
                }
            }
        }
        start += 128;
        for(int i = start; i < 128 + start; i++){
            if ((bytes[i] & 0xff) != 0) {
                List<Integer> ones = readBitmap(bytes[i]);
                int row = (i - start) / 4, col = ((i - start) % 4) * 8;
                for (Integer onePosition : ones){
                    Position p = new Position(row, col + onePosition, Config.BOARD_SIZE);
                    snake2.add(p);
                }
            }
        }
        return new Message.Builder()
                .type(type)
                .sequence(sequence)
                .apple(apple)
                .snake1(snake1)
                .snake2(snake2)
                .build();
    }

    private static List<Integer> readBitmap(byte b){
        List<Integer> result = new ArrayList<>();
        for(int i = 0; i < 8; i++){
            int val = (b >>> i) & 1;
            if (val == 1) { result.add(i); }
        }
        return result;
    }
}
