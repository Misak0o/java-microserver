import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.*;

import utils.*;


public class MicroServerSocket {


    public void start() throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(2134)) {
            System.out.println("Attend une nouvelle connexion au port 2134 ...");
            ExecutorService pool = Executors.newFixedThreadPool(5);
            Queue<Future<Boolean>> resultList = new LinkedBlockingQueue<>();
            boolean running = true;
            int nb_clients = 0;
            while (running) {
                Socket socket = serverSocket.accept();
                nb_clients++;
                Future<Boolean> result = pool.submit(new Worker(socket));
                resultList.add(result);
                if (nb_clients >= 10) {
                    System.out.println("Le serveur est fatigué, il va se reposer ...");
                    break;
                }
            }
            Future<Boolean> result;
            int nb_resultats = 0;
            while ((result = resultList.poll()) != null) {
                System.out.println(++nb_resultats + " résultat : " + result.get());
            }
            pool.shutdown();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    
    public static void main(String[] args) throws IOException {
        MicroServerSocket myMSS = new MicroServerSocket();
        myMSS.start();
    }
    
}
