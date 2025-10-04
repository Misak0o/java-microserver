package utils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

public class Worker implements Callable<Boolean> {
    private final Socket socket;

    public Worker(Socket socket) {
        this.socket = socket;
    }

    @Override
    public Boolean call() throws Exception {
        InputStream is = this.socket.getInputStream();
        byte[] bytes = new byte[is.available()];
        int byteRead = is.read(bytes);
        String request = new String(bytes);
        RequestParser rp = getContent(request);
        OutputStream os = socket.getOutputStream();
        
        String responseBody = "";
        Thread.sleep(10*1000);
        try {
            responseBody = switch (rp.method()) {
                case "GET" -> new String(Files.readAllBytes(Paths.get("."+rp.path())),
                                         StandardCharsets.UTF_8);
                case "POST" -> "You sent" + rp.body();
                default -> "Invalid method ! ;D";
            };
        } catch (IOException e) {
            String responseBody404 =
                    "<!doctype html>\n" +
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
            String response = "HTTP/1.1 404 NOT FOUND\n"
                    +"Content-Length: "+responseBody404.length()+"\n"
                    +"Content-Type: text/html\n\n"
                    + responseBody404;
            os.write(response.getBytes());
            socket.close();
            return true;
        }
        
        String response = "HTTP/1.1 200 OK\n"
            +"Content-Length: "+responseBody.length()+"\n"
            +"Content-Type: text/html\n\n"
            +responseBody;
        os.write(response.getBytes());
        socket.close();
        return true;
    }


    private RequestParser getContent(String request) throws IOException {
        Reader reader = new StringReader(request);
        BufferedReader buffer = new BufferedReader(reader);
        String line = buffer.readLine();
        StringBuilder currentHeaders = new StringBuilder();
        StringBuilder currentBody = new StringBuilder();
        
        String[] splitedLine = line.split(" ");
        String method = splitedLine[0];
        String path = splitedLine[1];

        while (line != null && !line.equals("\n")) {
            currentHeaders.append(line);
            line = buffer.readLine();
        }
        if (line != null) line = buffer.readLine();
        while (line != null) {
            currentBody.append(line);
            line = buffer.readLine();
        }
        String header = currentHeaders.toString();
        String body  = currentBody.toString();
        return new RequestParser(method, path, header, body);
    }


}
