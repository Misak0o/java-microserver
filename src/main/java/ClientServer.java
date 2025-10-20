import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientServer implements Runnable {

    AtomicInteger counter;

    public ClientServer(AtomicInteger counter) {
        this.counter = counter;
    }

    @Override
    public void run()/* throws IOException*/ {
        try (ServerSocket serverSocket = new ServerSocket(2134)) {
            ExecutorService pool = Executors.newFixedThreadPool(5);
            Queue<Future<Boolean>> resultList = new LinkedBlockingQueue<>();
            boolean running = true;
            int nbClients = 0;
            while (running) {
                System.out.println("Attend une nouvelle connexion au port 2134 ...");
                Socket socket = serverSocket.accept();
                nbClients++;
                Future<Boolean> result = pool.submit(new SocketClient(socket, counter));
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
