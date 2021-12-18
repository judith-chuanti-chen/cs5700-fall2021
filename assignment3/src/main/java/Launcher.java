/**
 * Entry point of the game.
 * Please edit the Configurations for Launcher before launching three instances of Launcher for
 * server, player 1 and player 2 respectively.
 * For each launcher, you can set the arguments in Configurations in the following order:
 * 1) start_server
 * 2) create 123 green 127.0.0.1 8181
 * 3) join 123 red 127.0.0.1 8282
 */
public class Launcher {
    private static final String START_SERVER = "start_server";
    private static final String CREATE = "create";
    private static final String JOIN = "join";
    public static void main(String[] args){
        System.out.println("Welcome to Snake Game!");
        if (args == null || args.length == 0){
            return;
        }
        if (args[0].equals(START_SERVER)){
            Server server = new Server();
            Thread serverThread = new Thread(server);
            serverThread.start();
        } else if (args[0].equals(CREATE) || args[0].equals(JOIN)){
            Message m = MessageReader.parseCommand(args);
            Client client = new Client(m);
            Thread clientThread = new Thread(client);
            clientThread.start();
        } else {
            return;
        }
    }
}
