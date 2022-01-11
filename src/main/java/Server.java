import handler.Handler;
import request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public void runServer() {
        try {
            serverSocket = new ServerSocket(9999);
            addHandler("GET", "/messages", (request, outputStream) -> {
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
            addHandler("POST", "/messages", (request, outputStream) -> {
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
            /*addHandler("GET", "public", (request, outputStream) -> {
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
            });*/
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientExecute(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientExecute(Socket clientSocket) {
        Thread thread = new Thread(new ClientExecutor(clientSocket, this));
        thread.start();
    }


    public void addHandler(String requestMethod, String path, Handler handler) {
        if (handlers.get(requestMethod) == null) {
            handlers.put(requestMethod, new ConcurrentHashMap<>());
        }
        handlers.get(requestMethod).put(path, handler);
    }

    public Map<String, Map<String, Handler>> getHandlers() {
        return handlers;
    }

    public List<String> getValidPaths() {
        return validPaths;
    }
}
