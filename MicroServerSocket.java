import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class MicroServerSocket {
    private String headers;
    private String body;

    public void start() throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(2134)) {
            System.out.println("Attend une nouvelle connexion au port 2134 ...");
            Socket socket = serverSocket.accept();

            System.out.println("Un client s'est connecté, on lit le contenu ...");
            InputStream is = socket.getInputStream();
            byte[] bytes = new byte[is.available()];
            int byteRead = is.read(bytes);
            String request = new String(bytes);
            getContent(request);

            System.out.println("Le client demande : " + request);
            String response = "Voilà ce que tu demandes : " + body;
            OutputStream os = socket.getOutputStream();
            os.write(response.getBytes());
        }
    }

    private void getContent(String request) throws IOException {
        Reader reader = new StringReader(request);
        BufferedReader buffer = new BufferedReader(reader);
        String line = buffer.readLine();
        StringBuilder currentHeaders = new StringBuilder();
        StringBuilder currentBody = new StringBuilder();
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
        headers = currentHeaders.toString();
        body  = currentBody.toString();
    }

    
    public static void main(String[] args) throws IOException {
        MicroServerSocket myMSS = new MicroServerSocket();
        myMSS.start();
    }
    
}
