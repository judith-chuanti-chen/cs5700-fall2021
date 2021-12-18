import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class Utils {
    private static String CONTENT_LENGTH = "Content-Length";
    private static int MAX_BUFFER_SIZE = 16;


    public static Integer evaluate(String expression){
        if (expression == null || expression.length() == 0) {
            return null;
        }
        char[] chars = expression.toCharArray();
        int num = 0, result = 0, sign = 1;
        Stack<Integer> stack = new Stack<>();
        for(int i = 0; i < chars.length; i++){
            if (chars[i] == '('){
                stack.add(result);
                stack.add(sign);
                result = 0;
                sign = 1;
            } else if (chars[i] == ')'){
                result += sign * num;
                if(stack.size() >= 2){
                    result = stack.pop() * result + stack.pop();
                    num = 0;
                }
                else {
                    //imbalanced parenthesis: (3-1)+2)+1
                    return null;
                }
            } else if (chars[i] == '+' || chars[i] == '-'){
                result += sign * num;
                num = 0;
                sign = chars[i] == '+' ? 1 : -1;
            } else if (Character.isDigit(chars[i])){
                num *= 10;
                num += Integer.parseInt(String.valueOf(chars[i]));
            } else {
                return null;
            }
        }
        System.out.println(stack.toString());
        if(!stack.isEmpty()) { return null; }
        result += sign * num;
        return result;
    }

    public static List<List<String>> recv(InputStream is) throws IOException {
        List<String> headers = new ArrayList<>();
        new StringBuilder();
        StringBuilder sb;
        List<List<String>> result = null;
        int contentLength = 0;
        // read headers
        try{
            do{
                sb = new StringBuilder();
                char c = (char) is.read();
                while(c != '\r') {
                    sb.append(c);
                    c = (char) is.read();
                }
             // read '/n'
                is.read();
                String header = sb.toString();
                if (header.length() > 0) { headers.add(sb.toString()); }
                if (header.contains(CONTENT_LENGTH)){
                    contentLength = Integer.parseInt(header.split(" ")[1]);
                }
            }while(sb.length() > 0);
            System.out.println(headers.toString());
            byte[] bodyBytes = new byte[contentLength];
            int bytesRead = 0;
            while (bytesRead < contentLength) {
                bytesRead += is.read(bodyBytes, bytesRead, Math.min(contentLength-bytesRead, MAX_BUFFER_SIZE));
            }
            String body = new String(bodyBytes);
            System.out.println(body);
            result = Arrays.asList(headers, Arrays.asList(body));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void send(OutputStream os, String response) throws IOException {
        byte[] data = response.getBytes();
        byte[] buffer = new byte[MAX_BUFFER_SIZE];
        int i = 0;
        while (i < data.length) {
            int len = Math.min(data.length-i, MAX_BUFFER_SIZE);
            for(int j = 0; j < len; j++){
                buffer[j] = data[i++];
            }
            try {
                os.write(buffer, 0, len);
                os.flush();
            } catch(Exception e) {
                e.printStackTrace();
                throw(e);
            }
            buffer = new byte[MAX_BUFFER_SIZE];
        }
        return;
    }

    public static String createResponse(String httpVersion, String status, String contentType, String body){
        String responseLine = String.format("%s %s\r\n", httpVersion, status);
        String response = responseLine;
        if (contentType != null) {
            String contentTypeHeader = String.format("Content-Type: %s\r\n", contentType);
            String contentLengthHeader = String.format("Content-Length: %s\r\n", body.length());
            response += contentTypeHeader + contentLengthHeader + "\r\n" + body;
        } else {
            response += "\r\n";
        }
        return response;
    }

    public static String createHTML(Counter counter){
        String header = "<h1>API count information</h1>\n";
        String evalExpression = "<h3>/api/evalexpression</h3>\n";
        long currentTime = System.currentTimeMillis();
        int lastMinuteEvals = counter.getCallsCountLastMinute(counter.getEvalCalls(), currentTime);
        int lastHourEvals = counter.getCallsCountLastHour(counter.getEvalCalls(), currentTime);
        int lastDayEvals = counter.getCallsCountLastDay(counter.getEvalCalls(), currentTime);
        int lifetimeEvals = counter.getCallsCountTotal(counter.getEvalCalls());
        evalExpression += String.format("<ul>\n" +
                "<li>last minute: %s</li>\n" +
                "<li>last hour: %s</li>\n" +
                "<li>last 24 hours: %s</li>\n" +
                "<li>lifetime: %s</li>\n" +
                "</ul>\n", lastMinuteEvals, lastHourEvals, lastDayEvals, lifetimeEvals);
        String getTime = "<h3>/api/gettime</h3>\n";
        int lastMinuteGetTime = counter.getCallsCountLastMinute(counter.getTimeCalls(), currentTime);
        int lastHourGetTime = counter.getCallsCountLastHour(counter.getTimeCalls(), currentTime);
        int lastDayGetTime = counter.getCallsCountLastDay(counter.getTimeCalls(), currentTime);
        int lifetimeGetTime = counter.getCallsCountTotal(counter.getTimeCalls());
        getTime += String.format("<ul>\n" +
                "<li>last minute: %s</li>\n" +
                "<li>last hour: %s</li>\n" +
                "<li>last 24 hours: %s</li>\n" +
                "<li>lifetime: %s</li>\n" +
                "</ul>\n", lastMinuteGetTime, lastHourGetTime, lastDayGetTime, lifetimeGetTime);
        String lastTenExpressions = "<h1>Last 10 expressions</h1>\n<ul>\n";
        for(String e : counter.getLastTenExpressions()){
            lastTenExpressions += String.format("<li>%s</li>\n", e);
        }
        lastTenExpressions += "</ul>\n";
        return header + evalExpression + getTime + lastTenExpressions;
    }
}
