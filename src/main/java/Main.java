import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        ClientServer myCS = new ClientServer();
        myCS.run();

        AdminServer myAS = new AdminServer();
        myAS.run();
    }
}
