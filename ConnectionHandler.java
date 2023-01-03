import java.io.*;
import java.net.Socket;
import java.util.*;


/*

O METODO CONNECTIONHANDLER TEM A FUNCAO DE GERIR TODAS AS LIGACOES REQUISITADAS AO CLIENT DE MODO A QUE VARIOS UTILIZADORS
POSSAM COMUNICAR AO MESMO TEMPO SEM QUE FIQUEM A ESPERA DE UMA RESPOSTA DO SERVIDOR DE CADA VEZ QUE ENVIAM/RECEBEM INFORMACAO

*/

public class ConnectionHandler implements Runnable {

    public static ArrayList<ConnectionHandler> connectionHandlers = new ArrayList<>(); // permite enviar mensagens a todos os clientes conectados, guarda então todos os ConnectionHandler criados
    public static Map <String, String> listaClientes = new LinkedHashMap<>();
    private Socket socket; // socket usada para establecer a conexao entre o cliente e o servidor
    private BufferedReader br; // ler dados, as mensagens enviadas pelos clientes
    private BufferedWriter bw; // enviar dados, as mensagens enviadas pelos clientes
    private String nomeUtilizador; // identificador de cada cliente
    private String ipCliente;
    private PrintWriter out; // printwrinter para imprimir mensagens no sistema

    public static ArrayList<String> messagesList = new ArrayList<>(); /* Arraylist para dar store as mensagens que vão sendo enviadas */
    /* public static ArrayList<String> listaClients = new ArrayList<>();  ArrayList com o intuito de guardar os utilizadores conectados */


    public ConnectionHandler(Socket socket) {
        try {
            this.socket = socket;
            // socket.setSoTimeout(120*1000); // timeout para o qual o servidor fica a espera de ouvir informação desta thread.
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream());
            this.nomeUtilizador = br.readLine();
            this.ipCliente = socket.getRemoteSocketAddress().toString();
            listaClientes.put(nomeUtilizador, ipCliente);
            connectionHandlers.add(this); // adiciona o novo utilizador à array list de modo a que estes possam ler e
                                      // enviar mensagens
            broadcast("Sessao: " + nomeUtilizador + " entrou no chat."); // DIFUNDE A MENSAGEM ATRAVÉS DO SERVIDOR A TODOS OS CLIENTES DE UM NOVO UTILIZADOR SE CONECTOU!
            updateUsersRequest(); // Imprime ao novo utilizador os utilizadores já conectados.
            messageRequest(); // Imprime ao novo utilizador as ultimas 10 mensagens do chat

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
                // a seguir uma restricao da lista com as mensagens nao poder conter mais do que 10 mensagens e ir atualizando a mesma
                // metodo auxiliado por https://www.tutorialkart.com/java/how-to-update-an-element-of-arraylist-in-java/
                if(messagesList.size() < 10) {
                    messagesList.add(messageFromClient);
                } else {
                    for(int k = 0; k < messagesList.size(); k++) {
                        messagesList.remove(0);
                        messagesList.set(k, messagesList.get(k+1));  //era k-1
                       //messagesList.set(k-1, messagesList.get(k));
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
        for (ConnectionHandler connectionHandler : connectionHandlers) { // iterar para todas as threads
            try { //Metodo do update da sessao auxiliado por Nam Ha Minh Java Chat App mais espeficamente as linhas 77 a 81
                if (!connectionHandler.nomeUtilizador.equals(nomeUtilizador)) {
                    if (mensagemParaEnviar.split(": ")[1].equalsIgnoreCase("SessionUpdate")) {
                        this.out.println("Sessao: ");
                        this.out.flush();
                        updateUsersRequest();
                        messageRequest();
                    } else {
                        connectionHandler.bw.write(mensagemParaEnviar);
                        connectionHandler.bw.newLine();
                        connectionHandler.bw.flush();
                    }
                }

            } catch (IOException e) {
                closeConnection(socket, br, bw);
            }
        }
    }


  // Método que mostra as últimas mensagens do chat //
  public void messageRequest() throws IOException{
    if(!messagesList.isEmpty()) {
        bw.write ("____________________");
        bw.newLine();
        this.bw.flush();
        this.bw.write("ULTIMAS MENSAGENS: ");
        bw.newLine();
        this.bw.flush();
        if (messagesList.size() <= 10) {
        for(int i = 0; i < messagesList.size(); i++){ // Ciclo que percorre a lista de mensagens e dá print ás mesmas //
            this.bw.write(messagesList.get(i));
            bw.newLine();
            this.bw.flush();
        }
        } else {
            for (int i = messagesList.size() - 10; i < messagesList.size(); i++) {
                this.bw.write(messagesList.get(i));
                bw.newLine();
                this.bw.flush();
            }
            this.bw.write("__________//__________");
            bw.newLine();
            this.bw.flush();
        }
    } else {
        this.bw.write ("____________________");
        this.bw.newLine();
        this.bw.flush();
        this.bw.write ("Sem historico de mensagens! ");
        this.bw.newLine();
        this.bw.flush();
        this.bw.write("__________//__________");
        bw.newLine();
        this.bw.flush();
    }
}

    public void updateUsersRequest() throws IOException {
        if (!listaClientes.isEmpty()) {
            bw.write ("____________________");
            bw.newLine();
            this.bw.flush();
            this.bw.write ("Utilizadores Online: ");
            bw.newLine();
            this.bw.flush();

            for (String nomes : listaClientes.keySet()) {
            this.bw.write (nomes + " (" + listaClientes.get(nomes) + ") ");
            bw.newLine();
            this.bw.flush();
        }
            bw.write("__________//__________");
            bw.newLine();
            this.bw.flush();
        } else {
            bw.write("____________________");
            bw.newLine();
            this.bw.flush();
            bw.write("Ainda nao ha utilizadores online. ");
            bw.newLine();
            this.bw.flush();
            bw.write("__________//__________");
            bw.newLine();
            this.bw.flush();
        }

    }

    public void closeThread() {
        connectionHandlers.remove(this);
        listaClientes.remove(nomeUtilizador);
        broadcast("Sessao: " + nomeUtilizador + " saiu do chat!");
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
