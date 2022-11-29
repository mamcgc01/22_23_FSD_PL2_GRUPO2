import java.util.*;
import java.io.*;
import java.net.Socket;

public class AgenteUtilizador {

    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private String nomeDeUtilizador;

    public AgenteUtilizador(Socket socket, String nomeDeUtilizador) {
        try {
            this.socket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.nomeDeUtilizador = nomeDeUtilizador;
        } catch (IOException e) {
            closeConnection(socket, br, bw);
        }
    }

    public void sendToGroupChat() {
        try {
            bw.write(nomeDeUtilizador);
            bw.newLine();
            bw.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String userMessage = scanner.nextLine();
                bw.write(nomeDeUtilizador + ": " + userMessage);
                bw.newLine();
                bw.flush();
            }
        } catch (Exception e) {
            closeConnection(socket, br, bw);
        }
    }

    public void listenToGroupChat() { //metodo para ouvir mensagens do grupo ao mesmo tempo que podemos estar a enviar novas mensagens sem ter de aguardar, dai ser necessario o Override e invocacao da Thread;
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
        }).start(); //cria o objeto
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

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in); //Criamos um scanner para poder ler o nome utilizador que serve como identificador
        System.out.println("Introduza o seu nome de utilizador!");
        String nomeDeUtilizador = scan.nextLine();
        //System.out.println("Introduza o enderco IP ao qual se deseja conectar");
        //String enderecoIP = scan.nextLine();
      /*   System.out.println("Introduza a porta a qual se quer conectar");
        Integer porta = scan.nextInt();
        while (porta != 8000) {
            System.out.println ("Porta incorreta! ");
            System.out.println ("Introduza uma nova porta: ");
            porta = scan.nextInt(); 
        }*/
        Socket sckt = new Socket("192.168.80.98", 8000); //Para conetar a diferentes computadores numa mesma rede especificar o valr "host" e a "porta"
        AgenteUtilizador agenteUtilizador = new AgenteUtilizador(sckt, nomeDeUtilizador);
        agenteUtilizador.listenToGroupChat(); // fica sempre a escuta das mensagens do grupo
        agenteUtilizador.sendToGroupChat(); // metodo para enviar mensagens!
    }
}
