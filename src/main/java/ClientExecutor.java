import handler.Handler;
import request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ClientExecutor implements Runnable{
    private Socket clientSocket;
    private Server server;

    public ClientExecutor(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try (
                final var in = clientSocket.getInputStream();
                final var out = new BufferedOutputStream(clientSocket.getOutputStream())) {
            Request request = new Request(in);
            if(server.getValidPaths().contains(request.getPath())){
                Map<String, Handler> handlerMap = server.getHandlers().get(request.getMethod());
                if (handlerMap == null) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    return;
                }

                Handler handler = handlerMap.get(request.getPath());
                //Если нет хендлера для данного метода, то отдаем запрашиваемый файл без обработки
                if (handler == null) {
                    var filePath = Path.of(".", "public", request.getPath());
                    var length = Files.size(filePath);
                    var mimeType = Files.probeContentType(filePath);
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, out);
                    out.flush();
                    return;
                }

                handler.handle(request, out);
            } else {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
