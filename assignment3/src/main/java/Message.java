import java.net.InetAddress;
import java.util.LinkedList;

public class Message {
    private int type;
    private String gameId;
    private String nickName;
    private InetAddress ip;
    private int port;
    private int dir;
    private int result;
    private String winner;
    private Position apple;
    private LinkedList<Position> snake1;
    private LinkedList<Position> snake2;
    private int sequence;

    public Message(Builder builder) {
        this.type = builder.type;
        this.gameId = builder.gameId;
        this.nickName = builder.nickName;
        this.ip = builder.ip;
        this.port = builder.port;
        this.dir = builder.dir;
        this.result = builder.result;
        this.winner = builder.winner;
        this.apple = builder.apple;
        this.snake1 = builder.snake1;
        this.snake2 = builder.snake2;
        this.sequence = builder.sequence;
    }

    public int getType() {
        return type;
    }

    public String getGameId() {
        return gameId;
    }

    public String getNickName() {
        return nickName;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getDir() {
        return dir;
    }

    public int getResult() {
        return result;
    }

    public String getWinner() {
        return winner;
    }

    public Position getApple() {
        return apple;
    }

    public LinkedList<Position> getSnake1() {
        return snake1;
    }

    public LinkedList<Position> getSnake2() {
        return snake2;
    }

    public int getSequence(){
        return sequence;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", gameId='" + gameId + '\'' +
                ", nickName='" + nickName + '\'' +
                ", ip=" + (ip == null ? "null" : ip.toString()) +
                ", port=" + port +
                ", dir=" + dir +
                ", result=" + result +
                ", winner='" + winner + '\'' +
                ", apple=" + (apple == null ? "null" : apple.toString()) +
                ", snake1=" + (snake1 == null ? "null" : snake1.toString()) +
                ", snake2=" + (snake2 == null ? "null" :  snake2.toString()) +
                ", sequence=" + sequence +
                '}';
    }

    public static class Builder{
        private int type;
        private String gameId;
        private String nickName;
        private InetAddress ip;
        private int port;
        private int dir;
        private int result;
        private String winner;
        private Position apple;
        private LinkedList<Position> snake1;
        private LinkedList<Position> snake2;
        private int sequence;

        public Builder() { }

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder gameId(String gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder nickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public Builder ip(InetAddress ip) {
            this.ip = ip;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder dir(int dir) {
            this.dir = dir;
            return this;
        }

        public Builder result(int result) {
            this.result = result;
            return this;
        }

        public Builder winner(String winner) {
            this.winner = winner;
            return this;
        }

        public Builder apple(Position apple) {
            this.apple = apple;
            return this;
        }

        public Builder snake1(LinkedList<Position> snake1) {
            this.snake1 = snake1;
            return this;
        }

        public Builder snake2(LinkedList<Position> snake2) {
            this.snake2 = snake2;
            return this;
        }

        public Builder sequence(int sequence) {
            this.sequence = sequence;
            return this;
        }

        public int getType() {
            return type;
        }

        public String getGameId() {
            return gameId;
        }

        public String getNickName() {
            return nickName;
        }

        public InetAddress getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public int getDir() {
            return dir;
        }

        public int getResult() {
            return result;
        }

        public String getWinner() {
            return winner;
        }

        public Position getApple() {
            return apple;
        }

        public LinkedList<Position> getSnake1() {
            return snake1;
        }

        public LinkedList<Position> getSnake2() {
            return snake2;
        }

        public int getSequence(){
            return sequence;
        }

        public Message build(){
            return new Message(this);
        }
    }
}
