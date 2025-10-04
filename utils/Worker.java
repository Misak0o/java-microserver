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
    
    public String readRequest() throws IOException {
        byte[] bytes = new byte[this.socket.getInputStream().available()];
        String request = "";
        InputStream is = this.socket.getInputStream();
        
        while (true) {
            int justRead = is.read(bytes);
            request += new String(bytes, 0, justRead, StandardCharsets.UTF_8);
            if (request.substring(request.length()-4).matches("\r\n\r\n"))
                break;
        }
        
        return request;
    }

    @Override
    public Boolean call() throws Exception {
        String request = readRequest();
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
            e.printStackTrace();
            throw e;
        }
        
        String response = "HTTP/1.1 200 OK\n"
            +"Content-Length: "+responseBody.length()+"\n"
            +"Content-Type: text/html\n\n"
            +responseBody;
        os.write(response.getBytes());
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
