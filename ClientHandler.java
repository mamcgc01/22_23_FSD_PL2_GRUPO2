import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.Buffer;
import java.util.*;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private String nomeUtilizador;

    public ClientHandler(Socket socket) {
        try {
            
            this.socket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.nomeUtilizador = br.readLine();
            clientHandlers.add(this);
            broadcastMessage("Servidor: " + nomeUtilizador + "entrou no chat");



        } catch (IOException e) {
            closeEverything(socket, br, bw);
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
    
}
