import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.nio.Buffer;
import java.util.*;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bf;
    private BufferedWriter bw;
    private String clientUsername;

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
    
}
