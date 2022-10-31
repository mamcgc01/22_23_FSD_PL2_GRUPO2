import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // permite enviar mensagens a todos os clientes conectados

    private Socket socket; // socket usada para establecer a conexao entre o cliente e o servidor
    private BufferedReader br; // ler dados,neste caso as mensagens enviadas pelo cliente
    private BufferedWriter bw; // enviar dados, neste caso as mensagens evnviadas pelo cliente
    private String nomeUtilizador; // identificador de cada cliente
    private PrintWriter pw; // printwrinter para imprimir mensagens

    public static ArrayList<String> listaMensagens = new ArrayList<>(); // Arraylist para dar store as mensagens que vão sendo enviadas


    public ClientHandler(Socket socket) {
        try {

            this.socket = socket;
            socket.setSoTimeout(120*1000); // timeout para o qual o servidor fica a espera de ouvir informação desta thread.
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            this.nomeUtilizador = br.readLine();
            clientHandlers.add(this); // adiciona o novo utilizador à array list de modo a que estes possam ler e
                                      // enviar mensagens
            broadcast("UPDATE: " + nomeUtilizador + " entrou no chat.");

        } catch (Exception e) {
            closeConnection(socket, br, bw);
        }
    }

    @Override
    public void run() {
        // O método run corre numa thread a parte com o intuito de "ouvir" as mensagens,
        // daí ser necessário @Override
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = br.readLine();
                // imprimimos neste metodo a restricao da lista com as mensagens nao poder conter mais do que 10 mensagens
                if(listaMensagens.size() < 10) {
                    listaMensagens.add(messageFromClient);
                } else {
                    for(int k = 0; k < listaMensagens.size(); k++) {
                        listaMensagens.remove(0);
                        listaMensagens.set(k, listaMensagens.get(k-1));
                        listaMensagens.set(k-1, listaMensagens.get(k));
                    }
                }
                broadcast(messageFromClient);
            } catch (IOException e) {
                closeConnection(socket, br, bw);
                break;
            }
        }
    }

    public void broadcast(String mensagemParaEnviar) {
        for (ClientHandler clientHandler : clientHandlers) { // iterar para todas as threads
            try {
                if (!clientHandler.nomeUtilizador.equals(nomeUtilizador)) {
                    if (mensagemParaEnviar.split(":")[1].equalsIgnoreCase("atualizarchat")) {
                        this.pw.println("----INFORMAÇÃO----");
                        this.pw.flush();
                       /* updateUsersRequest(); */
                        messageRequest();
                        this.pw.println("---------------");
                    /*}/*
                    /* if (mensagemParaEnviar.split(":")[1].equalsIgnoreCase("SAIR")) {
                        closeConnection(socket, br, bw); */
                    } else {
                        clientHandler.bw.write(mensagemParaEnviar);
                        clientHandler.bw.newLine();
                        clientHandler.bw.flush();
                    }
                }

            } catch (IOException e) {
                closeConnection(socket, br, bw);
            }
        }
    }


  public void messageRequest() {
        if (!listaMensagens.isEmpty()){
            this.pw.println("ULTIMAS MENSAGENS DOS UTILIZADORES: ");
            for (int i = 0; i < listaMensagens.size(); i++){
                this.pw.println(listaMensagens.get(i));
                this.pw.flush();
            }
            this.pw.println("------------------");
            this.pw.flush();
        }
    }

    /* public void updateUsersRequest() {
        for (ClientHandler clientHandler : clientHandlers) {
            this.pw.println("USERS ONLINE: ");
            for (int i = 0; i <clientHandlers.size(); i++){
                this.pw.println(clientHandlers.get(i));
                this.pw.flush();
            }
        }
    } */

    public void closeThread() {
        clientHandlers.remove(this);
        broadcast("SESSION_UPDATE: " + nomeUtilizador + " saiu do chat!");
    }

    public void closeConnection(Socket socket, BufferedReader br, BufferedWriter bw) {
        closeThread();
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
