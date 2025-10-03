import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import utils.*;


public class MicroServerSocket {


    public void start() throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(2134)) {
            System.out.println("Attend une nouvelle connexion au port 2134 ...");
            ExecutorService pool = Executors.newFixedThreadPool(5);
            Queue<Future<Integer>> resultList = new LinkedBlockingQueue<>();
            while (true) {
                Socket socket = serverSocket.accept();

            }


        }
    }

    
    public static void main(String[] args) throws IOException {
        MicroServerSocket myMSS = new MicroServerSocket();
        myMSS.start();
    }
    
}
