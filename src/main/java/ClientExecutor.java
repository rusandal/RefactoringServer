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
            String methhod = request.getMethod();
            String pathRequest = request.getPath();
            //Проверяем на валидность файлов. Если невалидный, отправляем badrequest
            if (pathRequest!=null & Server.VALID_PATH.contains(pathRequest)) {
                //Получаем мапу по методу
                Map<String, Handler> handlerMap = server.getHandlers().get(methhod);
                //Если мапы по методу нет, но файл в списке валидный файлов, то отправляем как есть и завершаем выполнение
                if (handlerMap == null) {
                    sendFile(out, pathRequest);
                    return;
                }
                Handler handler;
                //Проверяем путь на наличие в пути вложенного каталога
                //Если подкаталога в пути нет, то получаем хендлер или null
                //Если есть, то проверяем хендлер по каталогу
                if (!pathRequest.substring(1).contains("/")) {
                    handler = handlerMap.get(request.getPath());
                } else {
                    int index = pathRequest.indexOf('/', 1);
                    handler = handlerMap.get(pathRequest.substring(0, index));
                }
                //Если нет хендлера для данного метода, то отдаем хендлеру по умолчанию
                if (handler == null) {
                    if (handlerMap.containsKey("*")){
                        handler=handlerMap.get("*");
                        handler.handle(request, out);
                        return;
                    }
                }
                handler.handle(request, out);
            } else {
                badRequest(out);
                return;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void badRequest(BufferedOutputStream bufferedOutputStream) throws IOException {
        bufferedOutputStream.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        bufferedOutputStream.flush();
    }

    public void sendFile(BufferedOutputStream bufferedOutputStream, String requestPath) throws IOException {
        var filePath = Path.of(".", "public", requestPath);
        var length = Files.size(filePath);
        var mimeType = Files.probeContentType(filePath);
        bufferedOutputStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, bufferedOutputStream);
        bufferedOutputStream.flush();
    }
}
