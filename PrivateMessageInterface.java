import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrivateMessageInterface extends Remote {
    String sendMessage(String nomeUtilizador, String mensagemPrivada) throws RemoteException;

    String sendMessageSecure(String nomeUtilizador, String mensagemPrivada, String signature, String pubkey, String pos) throws RemoteException;
}
