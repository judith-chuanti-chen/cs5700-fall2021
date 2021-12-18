import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    private static int BUFFER_SIZE = 16;
    private static int OFF = 0;

    public static int evaluate(String expression){
        char[] chars = expression.toCharArray();
        int num = 0, result = 0, sign = 1;
        for(int i = 0; i < chars.length; i++){
            if (chars[i] == '+' || chars[i] == '-'){
                result += num * sign;
                num = 0;
                sign = chars[i] == '+' ? 1 : -1;
            } else {
                num *= 10;
                num += Integer.parseInt(String.valueOf(chars[i]));
            }
        }
        result += num * sign;
        return result;
    }

    /**
     * Encode a short (2 byte) into a list of bytes (size=2) using Big Endian
     * @param num a number to encode
     * @return a list of encoded bytes
     */
    public static List<Byte> encodeBigEndian(short num) {
        List<Byte> result = new ArrayList<>();
        result.add((byte) (num >>> 8));
        result.add((byte) ((num << 8) >>> 8));
        return result;
    }

    /**
     * Decode a list of bytes (size=2) using Big Endian and return a short (2 byte)
     * @param bytes a list of bytes to decode
     * @return decoded result
     */
    public static short decodeBigEndian(List<Byte> bytes){
        // & 0xff: force the bytes to stay in (0, 255) range
        // to safely convert bytes to unsigned short
        short num = (short)(((bytes.get(0) & 0xff)<< 8) | (bytes.get(1) & 0xff));
        return num;
    }

    public List<Byte> encodeData(String[] strings){
        short expressionCount = (short) strings.length;
        List<Byte> request = new ArrayList<>();
        request.addAll(encodeBigEndian(expressionCount));
        for(String e : strings){
            short numBytes = (short) e.getBytes().length;
            request.addAll(encodeBigEndian(numBytes));
            for (byte b : e.getBytes()){
                request.add(b);
            }
        }
        return request;
    }


    public static String[] recv(InputStream is) throws IOException {
        byte[] intBuffer = new byte[2];
        String[] result = null;
        try{
            // Read the first 2 bytes to find out the number of "(length, string)"
            if ((is.read(intBuffer, OFF, 2)) >= 2){
                int n = decodeBigEndian(Arrays.asList(intBuffer[0], intBuffer[1]));
                result = new String[n];
                // process each "(length, string)"
                for(int i = 0; i < n; i++){
                    intBuffer = new byte[2];
                    is.read(intBuffer, OFF, 2);
                    int strLen = decodeBigEndian(Arrays.asList(intBuffer[0], intBuffer[1]));
                    byte[] strBuffer = new byte[strLen];
                    int bytesRead = 0;
                    while(bytesRead < strLen){
                        bytesRead += is.read(strBuffer, bytesRead, Math.min(strLen - bytesRead, BUFFER_SIZE));
                    }
                    result[i] = new String(strBuffer);
                }
            }
            return result;
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static void send(OutputStream os, List<Byte> data) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int i = 0;
        while (i < data.size()) {
            int len = Math.min(data.size()-i, BUFFER_SIZE);
            for(int j = 0; j < len; j++){
                buffer[j] = data.get(i++);
            }
            try {
                os.write(buffer, OFF, len);
                os.flush();
            } catch(Exception e) {
                e.printStackTrace();
                throw(e);
            }
            buffer = new byte[BUFFER_SIZE];
        }
        return;
    }


    public static void main(String[] args){
        short num = 2689;
        Utils utils = new Utils();
        System.out.println(utils.decodeBigEndian(utils.encodeBigEndian(num)));
    }
}
