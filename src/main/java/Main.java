import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws IOException {
        ClientServer myCS = new ClientServer(new AtomicInteger());
        Thread clientThread = new Thread(myCS);
        clientThread.start();

        AdminServer myAS = new AdminServer(myCS);
        Thread adminThread = new Thread(myAS);
        adminThread.start();
    }
}
