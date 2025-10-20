import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws IOException {
        AtomicInteger counter = new AtomicInteger(0);
        Thread csThread = new Thread(new ClientServer(counter));
        csThread.run();
    }
}
