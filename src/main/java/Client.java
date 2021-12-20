import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("127.0.0.1", 9999);
            var in = new BufferedReader(new
                    InputStreamReader(clientSocket.getInputStream()));
            var out = new
                    PrintWriter(clientSocket.getOutputStream(), true);
            var reader = new BufferedReader(new InputStreamReader(System.in));
            out.println(reader.readLine());
            System.out.println(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
