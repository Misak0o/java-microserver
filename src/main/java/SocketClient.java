import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketClient implements Callable<Boolean> {
    private final Socket socket;
    private AtomicInteger counter;

    public SocketClient(Socket socket, AtomicInteger counter) {
        this.socket = socket;
        this.counter = counter;
    }

    public String readRequest() throws IOException {
        byte[] bytes = new byte[this.socket.getInputStream().available()];
        String request = "";
        InputStream is = this.socket.getInputStream();

        while (true) {
            int justRead = is.read(bytes);
            request += new String(bytes, 0, justRead, StandardCharsets.UTF_8);
            if (request.substring(request.length() - 4).equals("\r\n\r\n"))
                break;
        }

        return request;
    }

    @Override
    public Boolean call() throws Exception {
        String request = readRequest();
        RequestParser rp = getContent(request);
        Path path = Paths.get("." + rp.path());
        OutputStream os = socket.getOutputStream();

        String response = "";
        String responseBody = "";
        Thread.sleep(10 * 1000);
        if (Files.exists(path) && Files.isReadable(path)) {
            try {
                responseBody = switch (rp.method()) {
                case "GET" -> new String(Files.readAllBytes(path),
                                         StandardCharsets.UTF_8);
                case "POST" -> "You sent" + rp.body();
                default -> "Invalid method ! ;D";
                };
            } catch (IOException e) {
                throw new RuntimeException("Error while reading the file", e);
            }
            response = "HTTP/1.1 200 OK\n";
        } else {
            responseBody = "<!doctype html>\n" +
                    "<html>\n" +
                    "   <head>\n" +
                    "       <title>404 Not Found</title>\n" +
                    "       <meta charset=\"utf-8\">\n" +
                    "   </head>\n" +
                    "   <body>\n" +
                    "       <h1>404 Not Found</h1>\n" +
                    "            <p>The requested resource was not found on this server.</p>\n" +
                    "   </body>\n" +
                    "</html>\n";
            response = "HTTP/1.1 404 NOT FOUND\n";
        }

        response += "HTTP/1.1 200 OK\n"
                + "Content-Length: " + responseBody.length() + "\n"
                + "Content-Type: text/html\n\n"
                + responseBody;
        os.write(response.getBytes());
        socket.close();
        return true;
    }

    private RequestParser getContent(String request) throws IOException {
        Reader reader = new StringReader(request);
        BufferedReader buffer = new BufferedReader(reader);
        int nLines = 0;
        String line = buffer.readLine();
        StringBuilder currentHeaders = new StringBuilder();
        StringBuilder currentBody = new StringBuilder();

        String[] splitedLine = line.split(" ");
        String method = splitedLine[0];
        String path = splitedLine[1];

        while (line != null && !line.equals("\n")) {
            currentHeaders.append(line);
            line = buffer.readLine();
            ++nLines;
        }
        if (line != null)
            line = buffer.readLine();
        while (line != null) {
            currentBody.append(line);
            line = buffer.readLine();
        }
        String header = currentHeaders.toString();
        String body = currentBody.toString();
        counter.addAndGet(nLines);
        return new RequestParser(method, path, header, body);
    }

}
