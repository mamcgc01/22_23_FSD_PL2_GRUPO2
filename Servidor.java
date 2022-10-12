import java.io.IOException;
import java.net.ServerSocket;

public class Servidor {

    private ServerSocket svSocket;

    public Servidor(ServerSocket svSocket) {
        this.svSocket = svSocket;
    }

    public void iniciarSV() {
        try {

            while (!svSocket.isClosed()) {

               Socket socket = svSocket.accept();
               System.out.println("Entrou um novo utilizador no chat.");
               ClientHandler ClientHandler = new ClientHandler(socket);

               Thread thread = new Thread(clientHandler);
               thread.start();
            }

        } catch (IOException e) {

        }
    }

    public void fecharSVSocket() {
        try {
            if (ServerSocket != null) {
                serverSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Servidor servidor = new Servidor(serverSocket);
        servidor.iniciarSV();
    }
}