package utils;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.Callable;

public class Worker implements Callable<Integer> {
    private final Socket socket;

    Worker(Socket socket) {
        this.socket = socket;
    }

    @Override
    public Integer call() throws Exception {
        InputStream is = this.socket.getInputStream();
        byte[] bytes = new byte[is.available()];
        int byteRead = is.read(bytes);
        String request = new String(bytes);
        RequestParser rp = getContent(request);
        OutputStream os = socket.getOutputStream();
        String response = switch (rp.method()) {
            case "GET" -> """
                    <html>
                    <body>
                    
                    <h1>My First Heading</h1>
                    <p>My first paragraph.</p>
                    
                    </body>
                    </html>""";
            case "POST" -> "You sent" + rp.body();
            default -> "Invalid method ! ;D";
        };
        os.write(response.getBytes());
        return 0;
    }


    private RequestParser getContent(String request) throws IOException {
        Reader reader = new StringReader(request);
        BufferedReader buffer = new BufferedReader(reader);
        String line = buffer.readLine();
        StringBuilder currentHeaders = new StringBuilder();
        StringBuilder currentBody = new StringBuilder();
        String method = line.split(" ", 2)[0];
        System.out.println("Je commence à lire le contenu de la requête");
        while (!Objects.equals(line, "\n") && !Objects.equals(line, null)) {
            currentHeaders.append(line);
            line = buffer.readLine();
        }
        line = buffer.readLine();
        System.out.println("J'ai finis les headers, je commence à lire le body");
        while (!Objects.equals(line, "\n") && !Objects.equals(line, null)) {
            currentBody.append(line);
            line = buffer.readLine();
        }
        System.out.println("J'ai finis de lire la requête");
        String header = currentHeaders.toString();
        String body  = currentBody.toString();
        return new RequestParser(method, header, body);
    }


}
