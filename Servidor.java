import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Servidor {

    private ServerSocket svSocket;// objeto responsável por "receber novas conexões e criar uma socket para comunicar"

    public Servidor(ServerSocket svSocket) throws SocketException {
        this.svSocket = svSocket;
    }

    public void iniciarServidor() {

        try {

            while (!svSocket.isClosed()) {

               Socket socket = svSocket.accept();
               System.out.println("Entrou um novo utilizador no chat.");
               ConnectionHandler connectionHandler = new ConnectionHandler(socket);

               Thread thread = new Thread(connectionHandler);
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

    /* public void timerTask() {
        Método com o intuito de imprimir um updateUsersRequest() de 120 em 120 segundos.
    }*/

    public static void main(String[] args) throws IOException {
        System.out.println("SERVIDOR INICIADO: WAITING FOR CONNECTIONS ");
        ServerSocket serverSocket = new ServerSocket(8000);
        Servidor servidor = new Servidor(serverSocket); 
        servidor.iniciarServidor();
    }
}