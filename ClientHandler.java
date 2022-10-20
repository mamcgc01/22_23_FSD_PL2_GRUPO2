import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // permite enviar mensagens a todos os
                                                                               // clientes conectados
    private Socket socket; // socket usada para establecer a conexao entre o cliente e o servidor
    private BufferedReader br; // ler dados,neste caso as mensagens enviadas pelo cliente
    private BufferedWriter bw; // enviar dados, neste caso as mensagens evnviadas pelo cliente
    private String nomeUtilizador; // identificador de cada cliente

    public ClientHandler(Socket socket) {
        try {

            this.socket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.nomeUtilizador = br.readLine();
            clientHandlers.add(this); // adiciona o novo utilizador à array list de modo a que estes possam ler e
                                      // enviar mensagens
            SESSION_UPDATE("SESSION_UPDATE: " + nomeUtilizador + " entrou no chat.");

        } catch (IOException e) {
            fecharOperacao(socket, br, bw);
        }
    }

    @Override
    public void run() {
        // O método run corre num thread a parte com o intuito de "ouvir" as mensagens,
        // daí ser necessário @Override
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = br.readLine();
                SESSION_UPDATE(messageFromClient);

            } catch (IOException e) {
                fecharOperacao(socket, br, bw);
                break;
            }
        }
    }

    public void SESSION_UPDATE(String mensagemParaEnviar) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.nomeUtilizador.equals(nomeUtilizador)) {
                    clientHandler.bw.write(mensagemParaEnviar);
                    clientHandler.bw.newLine();
                    clientHandler.bw.flush();
                }
            } catch (IOException e) {
                fecharOperacao(socket, br, bw);
            }
        }
    }

    public void removerClientHandler() {
        clientHandlers.remove(this);
        SESSION_UPDATE("SESSION_UPDATE: " + nomeUtilizador + "saiu do chat!");
    }

    public void fecharOperacao(Socket socket, BufferedReader br, BufferedWriter bw) {
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

}
