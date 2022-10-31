import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Servidor {

    private ServerSocket svSocket;// objeto responsável por "receber novas conexões e criar uma socket para comunicar"
    static int DEFAULT_PORT = 2000;

    public Servidor(ServerSocket svSocket) throws SocketException {
        this.svSocket = svSocket;
    }

    public void iniciarServidor() {

        try {

            while (!svSocket.isClosed()) {

               Socket socket = svSocket.accept();
               System.out.println("Entrou um novo utilizador no chat.");
               ClientHandler clientHandler = new ClientHandler(socket);

               Thread thread = new Thread(clientHandler);
               thread.start();
            }

        } catch (IOException e) {
            fecharSVSocket();
        }
    }

    public void fecharSVSocket() {
        try {
            if (svSocket != null) {
                svSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2000);
        Servidor servidor = new Servidor(serverSocket); 
        servidor.iniciarServidor();
    }
}