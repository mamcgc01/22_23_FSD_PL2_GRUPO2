import java.util.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class AgenteUtilizador {

    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private String nomeUtilizador;

    public AgenteUtilizador(Socket socket, String nomeUtilizador) {
        try {
            this.socket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.nomeUtilizador = nomeUtilizador;
        } catch (IOException e) {
            closeConnection(socket, br, bw);
        }
    }

    public void sendToGroupChat() {
        try {
            bw.write(nomeUtilizador);
            bw.newLine();
            bw.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String mensagemParaEnviar = scanner.nextLine();
                bw.write(nomeUtilizador + ": " + mensagemParaEnviar);
                bw.newLine();
                bw.flush();
            }
        } catch (Exception e) {
            closeConnection(socket, br, bw);
        }
    }

    public void listenToGroupChat() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = br.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        closeConnection(socket, br, bw);
                    }
                }
            }
        }).start();
    }

    public void closeConnection(Socket socket, BufferedReader br, BufferedWriter bw) {
        try {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduza o seu nome de utilizador!");
        String nomeUtilizador = scanner.nextLine();
        Socket socket = new Socket("localhost", 2000);
        AgenteUtilizador agenteUtilizador = new AgenteUtilizador(socket, nomeUtilizador);
        agenteUtilizador.listenToGroupChat();
        agenteUtilizador.sendToGroupChat();
    }
}
