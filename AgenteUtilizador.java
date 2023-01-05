import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import java.io.*;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

public class AgenteUtilizador {

    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private String nomeDeUtilizador;
    String SERVICE_NAME = "/PrivateMessaging";
    public static HashMap<String, String> Clientes = new HashMap<>();
    public static HashMap<String, String> ClientesMP = new HashMap<>();
    private ArrayList<byte[]> lista = new ArrayList<>();
    private int contador = 0;
    private String ClienteIP;
    public static boolean conexao_terminada = false;
    static Timer timer = new Timer();
    static int SESSION_TIMEOUT = 240 * 1000;

    public AgenteUtilizador(Socket socket, String nomeDeUtilizador, String ClientIP, String receberMensagens) {
        try {
            this.socket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.nomeDeUtilizador = nomeDeUtilizador;
            if (receberMensagens.equalsIgnoreCase("sim"))
                ClientesMP.put(nomeDeUtilizador, ClientIP);

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

                    /* if(ClientesMP.containsValue(IP)) */

                    PrivateMessageInterface privateMessageInterface = (PrivateMessageInterface) LocateRegistry
                            .getRegistry(IP).lookup(SERVICE_NAME);
                    System.out.print("Introduza a mensagem que deseja enviar: ");
                    userMessage = scanner.nextLine();
                    privateMessageInterface.sendMessage(nomeDeUtilizador, userMessage);

                    /*
                     * else if(ClientesMP.containsValue(IP)==false) {
                     * System.out.print("O utilizador " + nomeDeUtilizador
                     * +" nao pretende receber mensagens privadas");
                     * bw.write(nomeDeUtilizador + ": " + userMessage);
                     * bw.newLine();
                     * bw.flush();
                     * }
                     */
                } else {
                    bw.write(nomeDeUtilizador + ": " + userMessage);
                    bw.newLine();
                    bw.flush();
                }

                if (userMessage.equals("MensagemSegura")) {
                    sendPrivateSecureMessage();
                }
                if (userMessage.equals("Verificar")) {
                    VerificarAssinatura();
                }
                if (userMessage.equals("Resumo")) {
                    sendResumo();
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

    private void sendResumo() throws RemoteException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, UnsupportedEncodingException {

        try {

            Scanner scanner = new Scanner(System.in);
            System.out.println("IP?");
            String IP = scanner.nextLine();
            System.out.println("Insira a mensagem que deseja fazer resumo: ");
            String message = scanner.nextLine();

            PrivateMessageInterface privateMessageSecureInterface = (PrivateMessageInterface) LocateRegistry
                    .getRegistry(IP)
                    .lookup(SERVICE_NAME);

            // Creating the MessageDigest object
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Passing data to the created MessageDigest Object
            md.update(message.getBytes());

            // Compute the message digest
            byte[] digest = md.digest();
            System.out.println(digest);

            // Converting the byte array in to HexString format
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < digest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & digest[i]));
            }
            System.out.println("Hex format : " + hexString.toString());
            String resumo = hexString.toString();
            String privateMessage = "Resumo: " + resumo;
            privateMessageSecureInterface.sendMessage(IP, privateMessage);
        } catch (Exception eh) {
            eh.printStackTrace();
        }

    }

    public void sendPrivateSecureMessage() throws RemoteException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, UnsupportedEncodingException {
        try {

            Scanner scanner = new Scanner(System.in);
            System.out.println("Qual o IP do utilizador a quem deseja enviar mensagem?");
            String IP = scanner.nextLine();
            PrivateMessageInterface privateMessageSecureInterface = (PrivateMessageInterface) LocateRegistry
                    .getRegistry(IP)
                    .lookup(SERVICE_NAME);
            System.out.print("Introduza a mensagem que deseja enviar: ");
            String userMessage = scanner.nextLine();

            // Creating the MessageDigest object
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Passing data to the created MessageDigest Object
            md.update(userMessage.getBytes());

            // Compute the message digest
            byte[] digest = md.digest();
            System.out.println(digest);

            // Converting the byte array in to HexString format
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < digest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & digest[i]));
            }
            System.out.println("Hex format : " + hexString.toString());
            String resumo = hexString.toString();

            // Creating a Signature object
            Signature sign = Signature.getInstance("SHA256withRSA");

            // Creating KeyPair generator object
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

            // Initializing the key pair generator
            keyPairGen.initialize(2048);

            // Generating the pair of keys
            KeyPair pair = keyPairGen.generateKeyPair();

            // Creating a Cipher object
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            // Initializing a Cipher object
            cipher.init(Cipher.ENCRYPT_MODE, pair.getPrivate());

            // Adding data to the cipher
            byte[] input = resumo.getBytes();
            cipher.update(input);

            // encrypting the data
            byte[] cipherText = cipher.doFinal();
            lista.add(cipherText);
            System.out.println(new String(cipherText, "UTF8"));
            String encript = new String(cipherText, "UTF8");
            String posicao = "Mensagem encriptada na posicao: " + contador;
            contador = contador + 1;

            PublicKey public_key = pair.getPublic();
            System.out.println("PUBLIC KEY::" + public_key);

            // converting public key to byte
            byte[] byte_pubkey = public_key.getEncoded();
            System.out.println("\nBYTE KEY::: " + byte_pubkey);

            // converting byte to String
            String str_key = Base64.getEncoder().encodeToString(byte_pubkey);
            // String str_key = new String(byte_pubkey,Charset.);
            System.out.println("\nSTRING KEY::" + str_key);

            privateMessageSecureInterface.sendMessageSecure(IP, userMessage, encript, str_key, posicao);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void VerificarAssinatura() throws RemoteException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException {

        try {

            Scanner scanner = new Scanner(System.in);
            System.out.println("IP?");
            String IP = scanner.nextLine();
            System.out.println("Insira a posicao: ");
            String posi = scanner.nextLine();
            System.out.println("Insira a chave publica ");
            String chave = scanner.nextLine();

            // converting string to Bytes
            byte[] byte_pubkey = Base64.getDecoder().decode(chave);
            System.out.println("BYTE KEY::" + byte_pubkey);

            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(byte_pubkey));
            System.out.println("Public KEY::" + publicKey);

            // Creating a Cipher object
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            // Initializing the same cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            // Adding data to the cipher
            int numeroConvertido = Integer.parseInt(posi);
            cipher.update(lista.get(numeroConvertido));

            // Decrypting the text
            byte[] decipheredText = cipher.doFinal();
            System.out.println(new String(decipheredText));
            String textoDecifrado = new String(decipheredText);

            PrivateMessageInterface privateMessageSecureInterface = (PrivateMessageInterface) LocateRegistry
                    .getRegistry(IP)
                    .lookup(SERVICE_NAME);

            privateMessageSecureInterface.sendMessage(IP, textoDecifrado);

        } catch (Exception eh) {
            eh.printStackTrace();
        }

    }

    // Método que em caso de erro fecha o socket de ligação e os buffers de entrada
    // e saída //
    public static void fecharTudo(Socket socket, BufferedReader br, BufferedWriter bw) {
        try {
            if (bw != null) {
                bw.close();
            }
            if (socket != null) { // Fechando o socket ira tambem fechar o Input e outputstream, assim nao e
                                  // necessario fechar//
                socket.close();
            }
            conexao_terminada = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in); // Criamos um scanner para poder ler o nome utilizador que serve como
                                               // identificador
        System.out.println("Introduza o seu nome de utilizador!");
        String nomeDeUtilizador = scan.nextLine();
        System.out.println("Pretende receber mensagens privadas de outros utilizadores (sim ou nao)");
        String receberMensagens = scan.nextLine();
        System.out.println("Introduza o IP da sua maquina");
        String ClientIP = scan.nextLine();
        Clientes.put(nomeDeUtilizador, ClientIP);
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

        Socket sckt = new Socket(enderecoIP, porta); // Para conetar a diferentes computadores numa mesma rede
        PrivateMessageImpl directMessage = null;
        try {
            directMessage = new PrivateMessageImpl();
        } catch (Exception e) {
        } // especificar o valor "host" e a "porta"

        AgenteUtilizador agenteUtilizador = new AgenteUtilizador(sckt, nomeDeUtilizador, ClientIP, receberMensagens);
        agenteUtilizador.bindRMI(directMessage, ClientIP);
        agenteUtilizador.listenToGroupChat(); // fica sempre a escuta das mensagens do grupo
        agenteUtilizador.sendToGroupChat(); // metodo para enviar mensagens!
    }
}
