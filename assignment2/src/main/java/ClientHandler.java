import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.spec.RSAOtherPrimeInfo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ClientHandler implements Runnable{
    private static String GET = "GET";
    private static String POST = "POST";
    private static String EVAL_URL = "/api/evalexpression";
    private static String TIME_URL = "/api/gettime";
    private static String STATUS_URL = "/status.html";
    private static String STATUS_200 = "200 OK";
    private static String STATUS_400 = "400 Bad Request";
    private static String STATUS_404 = "404 Not Found";
    private static String CONTENT_TEXT = "text/html";

    private Socket clientSocket;
    private Counter counter;

    public ClientHandler(Socket clientSocket, Counter counter) {
        this.clientSocket = clientSocket;
        this.counter = counter;
    }

    @Override
    public void run() {
        String client = String.format("[%s:%d]", clientSocket.getInetAddress(), clientSocket.getPort());
        System.out.println(String.format("Handle client %s", client));
        try {
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();

            List<List<String>> request = Utils.recv(is);
            List<String> headers = request.get(0);
            List<String> body = request.get(1);
            String requestLine = headers.get(0);
            String[] args = requestLine.split(" ");

            String method = args[0], url = args[1], httpVersion = args[2];
            String response = Utils.createResponse(httpVersion, STATUS_404, CONTENT_TEXT, "Invalid URL");
            long currentTime = System.currentTimeMillis();
            if (method.equals(GET)){
                // parse get
                if (url.equals(TIME_URL)){
                    counter.addTimeCalls(currentTime);
                    String result = new Date(currentTime).toString();
                    response = Utils.createResponse(httpVersion, STATUS_200, CONTENT_TEXT, result);
                } else if (url.equals(STATUS_URL)){
                    String html = Utils.createHTML(counter);
                    response = Utils.createResponse(httpVersion, STATUS_200, CONTENT_TEXT, html) ;
                }

            } else if (method.equals(POST)){
                // parse post
                if (url.equals(EVAL_URL)){
                    Integer result = Utils.evaluate(body.get(0));
                    if (result != null) {
                        // only counts this call when the expression is valid
                        counter.addEvalCalls(currentTime);
                        counter.addEvalExpressions(body.get(0));
                        response = Utils.createResponse(httpVersion, STATUS_200, CONTENT_TEXT, result.toString());
                    } else {
                        response = Utils.createResponse(httpVersion, STATUS_400, CONTENT_TEXT, "Unsupported arithmetic expression");
                    }
                }
            }
            Utils.send(os, response);
            is.close();
            os.close();
            clientSocket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
