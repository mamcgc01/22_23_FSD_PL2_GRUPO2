import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Servidor {

    private ServerSocket svSckt;// objeto responsável por "receber novas conexões e criar uma socket para comunicar"

    public Servidor(ServerSocket svSckt) throws SocketException {
        this.svSckt = svSckt;
    }

    public void iniciarServidor() {

        try {

            while (!svSckt.isClosed()) {

               Socket sckt = svSckt.accept();
               System.out.println("Entrou um novo utilizador no chat.");
               ConnectionHandler connectionHandler = new ConnectionHandler(sckt);

               Thread thread = new Thread(connectionHandler);
               thread.start();
               System.out.println(" PRIMA CRTL + C para terminar o programa/sessao ");
            }

        } catch (IOException e) {
            fecharSVSocket();
        }
    }

    public void fecharSVSocket() {
        try {
            if (svSckt != null) {
                svSckt.close();
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
        ServerSocket serverSckt = new ServerSocket(8000);
        Servidor servidor = new Servidor(serverSckt); 
        servidor.iniciarServidor();
    }
}