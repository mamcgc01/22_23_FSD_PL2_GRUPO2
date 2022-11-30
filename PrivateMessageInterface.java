import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrivateMessageInterface extends Remote {
    String sendMessage(String nomeUtilizador, String mensagemPrivada) throws RemoteException;
}
