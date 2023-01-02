import java.util.*;
import java.io.*;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class AgenteUtilizador {

    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private String nomeDeUtilizador;
    String SERVICE_NAME = "/PrivateMessaging";

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
                if (userMessage.equals("MensagemPrivada")) {
                    System.out.println("Qual o IP do Utilizador a quem deseja enviar mensagem?");
                    String IP = scanner.nextLine();
                    PrivateMessageInterface privateMessageInterface = (PrivateMessageInterface) LocateRegistry
                            .getRegistry(IP).lookup(SERVICE_NAME);
                    System.out.print("Introduza a mensagem que deseja enviar: ");
                    userMessage = scanner.nextLine();
                    privateMessageInterface.sendMessage(nomeDeUtilizador, userMessage);
                } else {
                    bw.write(nomeDeUtilizador + ": " + userMessage);
                    bw.newLine();
                    bw.flush();
                }

            }
        } catch (Exception e) {
            closeConnection(socket, br, bw);
        }
    }

    public void listenToGroupChat() { // metodo para ouvir mensagens do grupo ao mesmo tempo que podemos estar a
                                      // enviar novas mensagens sem ter de aguardar, dai ser necessario o Override e
                                      // invocacao da Thread;
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
        }).start(); // cria o objeto
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

    private void bindRMI(PrivateMessageImpl directMessageImpl, String IP) throws RemoteException {

        System.getProperties().put("java.security.policy", "./server.policy");

        /*
         * if (System.getSecurityManager() == null) {
         * System.setSecurityManager(new SecurityManager());
         * }
         */

        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {

        }
        try {
            LocateRegistry.getRegistry(IP, 1099).rebind(SERVICE_NAME, directMessageImpl);
        } catch (RemoteException e) {
            System.out.println("Registry not found");
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in); // Criamos um scanner para poder ler o nome utilizador que serve como
                                               // identificador
        System.out.println("Introduza o seu nome de utilizador!");
        String nomeDeUtilizador = scan.nextLine();
        System.out.println("Introduza o endereco IP ao qual se deseja conectar");
        String enderecoIP = scan.nextLine();
        System.out.println("Introduza a porta a qual se quer conectar");
        String port = scan.nextLine();
        Integer porta = Integer.parseInt(port);
        while (porta != 8000) {
            System.out.println("Porta incorreta! ");
            System.out.println("Introduza uma nova porta: ");
            port = scan.nextLine();
            porta = Integer.parseInt(port);
        }
        System.out.println("Introduza o IP da sua maquina");
        String ClientIP = scan.nextLine();

        Socket sckt = new Socket(enderecoIP, porta); // Para conetar a diferentes computadores numa mesma rede
        PrivateMessageImpl directMessage = null;
        try {
            directMessage = new PrivateMessageImpl();
        } catch (Exception e) {
            // TODO: handle exception
        } // especificar o valor "host" e a "porta"

        AgenteUtilizador agenteUtilizador = new AgenteUtilizador(sckt, nomeDeUtilizador);
        agenteUtilizador.bindRMI(directMessage, ClientIP);
        agenteUtilizador.listenToGroupChat(); // fica sempre a escuta das mensagens do grupo
        agenteUtilizador.sendToGroupChat(); // metodo para enviar mensagens!
    }
}
