import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class MessageWriter {
    // TODO: when converting idLength to int, need to do idLength & 0XFF
    public static byte[] createOrJoinGameMsg(Message m){
        int idLength = m.getGameId().length();
        int nameLength = m.getNickName().length();
        int arrLen = 3 + idLength + nameLength + 4 + 2;
        byte[] msg = new byte[arrLen];
        msg[0] = (byte) m.getType();
        msg[1] = (byte) idLength;
        msg[2] = (byte) nameLength;
        byte[] ipInBytes = m.getIp().getAddress();
        byte[] portInBytes = BigInteger.valueOf(m.getPort()).toByteArray();
        int i = 3;
        for (byte b : m.getGameId().getBytes()) { msg[i++] = b; }
        for (byte b : m.getNickName().getBytes()) { msg[i++] = b; }
        for (byte b : ipInBytes) { msg[i++] = b; }
        for (byte b : portInBytes) { msg[i++] = b; }
        return msg;
    }

    public static byte[] changeDirectionMsg(Message m){
        int idLength = m.getGameId().length();
        int nameLength = m.getNickName().length();
        int arrLen = 3 + idLength + nameLength + 1;
        byte[] msg = new byte[arrLen];
        msg[0] = (byte) m.getType();
        msg[1] = (byte) idLength;
        msg[2] = (byte) nameLength;
        int i = 3;
        for (byte b : m.getGameId().getBytes()) { msg[i++] = b; }
        for (byte b : m.getNickName().getBytes()) { msg[i++] = b; }
        msg[arrLen-1] = (byte) m.getDir();
        System.out.println("changeDirectionMsg: " + m.toString());
        return msg;
    }

    public static byte[] waitForSecondUserMsg(){
        byte[] msg = {4};
        return msg;
    }

    public static byte[] waitForGameStartMsg(){
        byte[] msg = {5};
        return msg;
    }

    public static byte[] gameOverMsg(byte result, String winnerName){
        byte[] msg;
        if (winnerName == null)  {
            msg = new byte[]{6, result};
        } else {
            int nameLength =  winnerName.length();
            int arrLength = 3 + nameLength;
            msg = new byte[arrLength];
            msg[0] = 6;
            msg[1] = result;
            msg[2] = (byte) nameLength;
            int i = 3;
            for (byte b : winnerName.getBytes()) { msg[i++] = b; }
        }
        System.out.println("gameOverMsg: " + msg.toString());
        return msg;
    }

    public static byte[] gameStateMsg(int sequence, Position apple, LinkedList<Position> snake1, LinkedList<Position> snake2){
        int arrLength = 260;
        byte[] msg = new byte[arrLength];
        msg[0] = 7;
        msg[1] = (byte) sequence;
        msg[2] = (byte) apple.getRow();
        msg[3] = (byte) apple.getCol();
        int start = 4;
        for (int i = 0; i < snake1.size(); i++){
            Position p = snake1.get(i);
            int x = p.getRow(), y = p.getCol() / 8, digit = p.getCol() % 8;
            int idx = start + x * 4 + y;
            msg[idx] += (byte) Math.pow(2, digit);
        }
        start += 128;
        for (int i = 0; i < snake2.size(); i++){
            Position p = snake2.get(i);
            int x = p.getRow(), y = p.getCol() / 8, digit = p.getCol() % 8;
            int idx = start + x * 4 + y;
            msg[idx] += (byte) Math.pow(2, digit);

        }
        System.out.println("gameStateMsg: apple=" + apple.toString() + ", snake1=" + snake1.toString() +
                ", snake2=" + snake2.toString());
        return msg;
    }

}
