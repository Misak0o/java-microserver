import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.*;

public class AdminServer implements Runnable {

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8090)) {
            ExecutorService pool = Executors.newFixedThreadPool(5);
            Queue<Future<Boolean>> resultList = new LinkedBlockingQueue<>();
            boolean running = true;
            int nbClients = 0;
            while (running) {
                System.out.println("Attend une nouvelle connexion au port 8090 ...");
                Socket socket = serverSocket.accept();
                nbClients++;
                Future<Boolean> result = pool.submit(new SocketClient(socket));
                resultList.add(result);
                if (nbClients >= 10) {

                    System.out.println("Le serveur est fatigué, il va se reposer ...");
                    running = false;
                }
            }
            Future<Boolean> result;
            int nbResultats = 0;
            while ((result = resultList.poll()) != null) {
                System.out.println(++nbResultats + " résultat : " + result.get());
            }
            pool.shutdown();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
