import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server-side program, responsible for establishing connection,
 * and handling multi-threaded requests from clients
 */
public class Server {
    public static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        System.out.println("Threaded echo server");
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        try {
            System.out.println("Start to accept incoming connections");
            // while(true) allows a socket to continuously accept incoming connection
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }
}
