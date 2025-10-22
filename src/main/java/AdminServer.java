import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AdminServer implements Runnable {

    private ClientServer cS;

    public AdminServer(ClientServer cS) {
        this.cS = cS;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8090)) {
            System.out.println("Attend une nouvelle connexion au port 8090 ...");
            Socket socket = serverSocket.accept();
            System.out.println("Arrêt du serveur en cours ...");
            cS.shutdownServer();
            socket.close();
            serverSocket.close();
            System.out.println("Serveur arrêté");
            System.exit(0);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
