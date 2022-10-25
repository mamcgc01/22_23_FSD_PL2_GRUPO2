import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private ServerSocket svSocket; // objeto responsável por "escutar novas conexões e criar uma socket para comunicar"

    public Servidor(ServerSocket svSocket) {
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

    public void SESSION_UPDATE () {
        try {
            while (true) {
                for(int i=0; i < ClientHandler.clientHandlers.size(); i++){
                    System.out.println(ClientHandler.clientHandlers.get(i));
                }

                Thread.sleep(120 * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2000);
        Servidor servidor = new Servidor(serverSocket);
        servidor.SESSION_UPDATE();
        servidor.iniciarServidor();
    }
}