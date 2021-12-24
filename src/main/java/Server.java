import handler.Handler;
import request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static ServerSocket serverSocket;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private static Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        Server.runServer();
    }

    public static void runServer() {
        try {
            serverSocket = new ServerSocket(9999);
            Server.addHandler("GET", "/messages", (request, outputStream) -> {
                final var filePath = Path.of(".", "public", request.getPath());
                final var mimeType = Files.probeContentType(filePath);

                final var length = Files.size(filePath);
                outputStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, outputStream);
                outputStream.flush();
            });
            Server.addHandler("POST", "/messages", (request, outputStream) -> {
                final var filePath = Path.of(".", "public", request.getPath());
                final var mimeType = Files.probeContentType(filePath);

                final var length = Files.size(filePath);
                outputStream.write((
                                "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, outputStream);
                outputStream.flush();
            });
            Server.addHandler("GET", "public", (request, outputStream) -> {
                final var filePath = Path.of(".", request.getPath());
                final var mimeType = Files.probeContentType(filePath);

                final var length = Files.size(filePath);
                outputStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, outputStream);
                outputStream.flush();
            });
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientExecute(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clientExecute(Socket clientSocket) {
        threadPool.execute(() -> {
            try (
                    final var in = clientSocket.getInputStream();
                    final var out = new BufferedOutputStream(clientSocket.getOutputStream())) {
                Request request = new Request(in);

                Map<String, Handler> handlerMap = handlers.get(request.getMethod());
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

            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }


    public static void addHandler(String requestMethod, String path, Handler handler) {
        if (handlers.get(requestMethod) == null) {
            handlers.put(requestMethod, new ConcurrentHashMap<>());
        }
        handlers.get(requestMethod).put(path, handler);
    }
}
