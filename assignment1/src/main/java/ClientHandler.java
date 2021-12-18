import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * Server-side program, acts as a runnable target for a new thread, responsible for
 * reading and replying client's messages
 */
public class ClientHandler implements Runnable{
    private Socket clientSocket;
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        String client = String.format("[%s:%d]", clientSocket.getInetAddress(), clientSocket.getPort());
        System.out.println(String.format("Handle client %s", client));
        try{
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            Utils utils = new Utils();
            String[] expressions = utils.recv(is);
            System.out.println("Received expressions: " + Arrays.toString(expressions));
            String[] evalResults = new String[expressions.length];
            for(int i = 0; i < expressions.length; i++){
                evalResults[i] = Integer.toString(utils.evaluate(expressions[i]));
            }
            System.out.println("Evaluation results: " + Arrays.toString(evalResults));
            List<Byte> response = utils.encodeData(evalResults);
            Utils.send(os, response);
            is.close();
            os.close();
            clientSocket.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{

        }
    }
}
