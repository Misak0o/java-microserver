import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientServer implements Runnable {

    private AtomicInteger counter;
    private ExecutorService pool;
    private Queue<Future<Boolean>> resultList;
    private volatile Boolean running;

    public ClientServer(AtomicInteger counter) {
        this.counter = counter;
        this.pool = Executors.newFixedThreadPool(5);
        this.resultList = new LinkedBlockingQueue<>();
        this.running = true;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(2134)) {
            while (running) {
                System.out.println("Attend une nouvelle connexion au port 2134 ...");
                Socket socket = serverSocket.accept();
                Future<Boolean> result = this.pool.submit(new SocketClient(socket, counter));
                resultList.add(result);
            }
            Future<Boolean> result;
            int nbResultats = 0;
            while ((result = this.resultList.poll()) != null) {
                System.out.println(++nbResultats + " résultat : " + result.get());
            }
            this.pool.shutdown();
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public AtomicInteger shutdownServer() {
        this.running = false;
        this.pool.shutdown();
        return this.counter;
    }

}
